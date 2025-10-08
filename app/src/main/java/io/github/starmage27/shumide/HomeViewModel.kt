package io.github.starmage27.shumide

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.AndroidViewModel
import io.github.starmage27.shumide.Utils.getDiffRange
import io.github.starmage27.shumide.Utils.toStyleSpans
import io.github.starmage27.shumide.data.StyleSpan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uniffi.treesitterbridge.DiffRange
import uniffi.treesitterbridge.Highlight
import uniffi.treesitterbridge.TsBridge
import uniffi.treesitterbridge.TsLang
import kotlin.random.Random

@OptIn(FlowPreview::class)
class HomeViewModel(
    private val tsBridge: TsBridge,
    application: Application
) : AndroidViewModel(application) {
    private val tag = "HomeViewModel"
    private val coroutineScope = CoroutineScope(Dispatchers.Unconfined)
    private val rng: Random = Random(System.currentTimeMillis())

    private val styles: List<SpanStyle> = List(size = 1024) { i ->
        val hue = rng.nextFloat() * 360f
        val color = Color.hsv(hue, saturation = 0.8f, value = 1f)
        SpanStyle(color)
    }

    private var oldText: CharSequence = ""
    val textFieldState = TextFieldState()
    private val textSnapshot = snapshotFlow { textFieldState.text }


    private var _layoutResult: MutableStateFlow<TextLayoutResult?> = MutableStateFlow(null)
    val layoutResult = _layoutResult.asStateFlow()
    fun setLayoutResult(newLayoutResult: TextLayoutResult?) = _layoutResult.update { newLayoutResult }
    private var _textFieldSize = MutableStateFlow(Size(1f, 1f))
    val textFieldSize = _textFieldSize.asStateFlow()
    fun setTextFieldSize(newSize: Size) = _textFieldSize.update { newSize }
    private var _scrollValue = MutableStateFlow(0)
    val scrollValue = _scrollValue.asStateFlow()
    val scrollSnapshot = snapshotFlow { scrollValue.value }
    fun setScrollValue(newScrollValue: Int) = _scrollValue.update { newScrollValue }

    private var _selectedLanguage = MutableStateFlow(TsLang.RUST)
    val selectedLanguage = _selectedLanguage.asStateFlow()

    private var _styleSpans = MutableStateFlow(emptyList<StyleSpan>())
    val styleSpans = _styleSpans.asStateFlow()
    private var _visibleStyleSpans = MutableStateFlow(emptyList<StyleSpan>())
    val visibleStyleSpans = _visibleStyleSpans.asStateFlow()

    private var _textStyle = MutableStateFlow(TextStyle.Default.copy(
        fontFamily = FontFamily.Monospace,
        color = Color.White,
    ))
    val textStyle = _textStyle.asStateFlow()

    init {
        coroutineScope.launch {
            tsBridge.setLanguage(selectedLanguage.value)
            runHighlighting()
        }
        coroutineScope.launch {
            scrollValue.debounce(50).collectLatest {
                updateVisibleStyles()
            }
        }
        coroutineScope.launch {
            runVisibleStyles()
        }
    }

    fun updateVisibleStyles() {
        if (layoutResult.value == null) return
        coroutineScope.async {
            val firstVisibleLine = layoutResult.value!!.getLineForVerticalPosition(_scrollValue.value.toFloat())
            val lastVisibleLine = layoutResult.value!!.getLineForVerticalPosition(_scrollValue.value + _textFieldSize.value.height)

            val newStartOffset = layoutResult.value!!.getLineStart(firstVisibleLine)
            val newEndOffset = layoutResult.value!!.getLineEnd(lastVisibleLine)

            _visibleStyleSpans.update {
                styleSpans.value.filter { styleSpan ->
                    val invisible = (styleSpan.start >= newEndOffset) || (styleSpan.end <= newStartOffset)
                    !invisible
                }
            }
        }
    }

    private suspend fun logKinds() {
        Log.i(tag, "Logging kinds...")
        val kinds = tsBridge.getKindsForSelectedLanguage()
        Log.i(tag, "Kinds: $kinds")
    }

    fun setLanguage(lang: TsLang) {
        coroutineScope.launch {
            try {
                tsBridge.setLanguage(lang)
                //logKinds()
                _selectedLanguage.update { lang }
                highlight(oldText, null)
            } catch(e: Exception) {
                Log.e(tag, e.message, e)
            }
        }
    }

    private suspend fun highlight(
        text: CharSequence,
        diffRange: DiffRange? = getDiffRange(old = oldText, new = text),
    ) {
        val highlights = parse(sourceString = text.toString(), diffRange)
        _styleSpans.update {
            highlights.toStyleSpans { kind ->
                val kind = if (kind != UShort.MAX_VALUE) kind.toInt() else 0

                styles[kind]
            }
        }
        oldText = text
    }

    @OptIn(FlowPreview::class)
    suspend fun runHighlighting() {
        textSnapshot.distinctUntilChanged().collectLatest { text ->
            highlight(text)
        }
    }

    suspend fun runVisibleStyles() {
        styleSpans.collectLatest {
            updateVisibleStyles()
        }
    }

    private suspend fun parse(
        sourceString: String,
        diffRange: DiffRange? = null
    ): List<Highlight> {
        val highlights: List<Highlight> = if (diffRange == null) {
            tsBridge.parseEverything(sourceString)
        } else {
            val changedPart = sourceString
                .substring(range = diffRange.start until diffRange.newEnd)
            tsBridge.parseChanges(changedPart, diffRange)
        }
        return highlights
    }

    fun loadFile(uri: Uri?, context: Context = getApplication()) {
        if (uri == null) return

        val content = context.contentResolver.openInputStream(uri)?.bufferedReader().use {
            it?.readText()
        }

        if (content == null) {
            Log.e(tag, "Could not load file")
            return
        }

        textFieldState.clearText()
        textFieldState.edit {
            append(content)
        }
    }

    fun saveFile(uri: Uri?, context: Context = getApplication()) {
        if (uri == null) return

        val outputStream = context.contentResolver.openOutputStream(uri)
        if (outputStream == null) {
            Log.e(tag, "OutputStream is null")
            return
        }

        val writer = outputStream.bufferedWriter()
        try {
            writer.write(textFieldState.text.toString())
        } catch(e: Exception) {
            Log.e(tag, "Error writing to a file $e")
        } finally {
            writer.close()
        }
    }
}