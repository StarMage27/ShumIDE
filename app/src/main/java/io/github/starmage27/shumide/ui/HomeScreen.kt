package io.github.starmage27.shumide.ui

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import io.github.starmage27.shumide.HomeViewModel
import io.github.starmage27.shumide.OutputTransformationHighlight
import io.github.starmage27.shumide.R
import io.github.starmage27.shumide.data.StyleSpan
import io.github.starmage27.shumide.ui.theme.ShumIDETheme
import uniffi.treesitterbridge.TsLang

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navController: NavController,
) {
    val textFieldState = viewModel.textFieldState

    val selectedLanguage = viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val textStyle = viewModel.textStyle.collectAsStateWithLifecycle()
    val visibleStyleSpans = viewModel.visibleStyleSpans.collectAsStateWithLifecycle()

    HomeUI(
        textFieldState = textFieldState,
        textStyle = textStyle.value,
        selectedLanguage = selectedLanguage.value,
        onSelectLanguage = { tsLang -> viewModel.setLanguage(tsLang) },
        onLoadFile = { uri -> viewModel.loadFile(uri) },
        onSaveFile = { uri -> viewModel.saveFile(uri) },
        spanStylesProvider = { visibleStyleSpans.value },
        onTextLayoutChanged = { layoutResult -> viewModel.setLayoutResult(layoutResult) },
        onTextFieldSizeChanged = { size -> viewModel.setTextFieldSize(size) },
        onScroll = { scrollValue -> viewModel.setScrollValue(scrollValue) },
        onNavigate = { route -> navController.navigate(route) },
    )

//    val paddings = getPaddingsForInsets()
//
//    /// Debug text
//    OutlinedText(
//        modifier = Modifier.padding(paddings).padding(top = 64.dp).fillMaxWidth(),
//        text = "Start offset: ${startOffset.value}\n" +
//            "End offset: ${endOffset.value}",
//        fontSize = 24.sp,
//        textAlign = TextAlign.End,
//        fontFamily = FontFamily.Monospace,
//        fillColor = Color.Black,
//        outlineColor = Color.White
//    )
}

@Composable
fun HomeUI(
    modifier: Modifier = Modifier,
    textFieldState: TextFieldState = TextFieldState("Test"),
    textStyle: TextStyle = TextStyle.Default.copy(fontFamily = FontFamily.Monospace),
    selectedLanguage: TsLang = TsLang.KOTLIN,
    onSelectLanguage: (tsLang: TsLang) -> Unit = { _ -> },
    onLoadFile: (Uri?) -> Unit = { _ -> },
    onSaveFile: (Uri?) -> Unit = { _ -> },
    spanStylesProvider: () -> List<StyleSpan> = { emptyList() },
    onTextLayoutChanged: (TextLayoutResult?) -> Unit = { _ -> },
    onTextFieldSizeChanged: (Size) -> Unit = { _ -> },
    onScroll: (Int) -> Unit = { _ -> },
    onNavigate: (route: Any) -> Unit = { _ -> },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            HomeTopBar(
                onLoadFile = { uri -> onLoadFile(uri) },
                onSaveFile = { uri -> onSaveFile(uri) },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            EnumDropdownMenu(
                label = "Language",
                selectedElement = selectedLanguage,
                onSelected = { tsLang ->
                    onSelectLanguage(tsLang)
                }
            )
            CodeTextField(
                state = textFieldState,
                textStyle = textStyle,
                outputTransformation = OutputTransformationHighlight { spanStylesProvider() },
                onTextLayoutChanged = { onTextLayoutChanged(it) },
                onSizeChanged = { onTextFieldSizeChanged(it) },
                onScroll = { onScroll(it) }
            )
        }
    }
}

@Composable
fun CodeTextField(
    modifier: Modifier = Modifier,
    state: TextFieldState,
    textStyle: TextStyle,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.MultiLine(),
    outputTransformation: OutputTransformation? = null,
    onTextLayoutChanged: (layout: TextLayoutResult?) -> Unit,
    onSizeChanged: (size: Size) -> Unit,
    onScroll: (Int) -> Unit,
) {
    val density = LocalDensity.current

    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    LaunchedEffect(verticalScrollState.value) {
        onScroll(verticalScrollState.value)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { newSize ->
                with(density) {
                    onSizeChanged(newSize.toSize())
                }
            }
        ,
    ) {
        BasicTextField(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScrollState)
                .horizontalScroll(horizontalScrollState)
            ,
            state = state,
            lineLimits = lineLimits,
            textStyle = textStyle,
            onTextLayout = { textLayoutProvider ->
                onTextLayoutChanged(textLayoutProvider())
            },
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
            outputTransformation = outputTransformation
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    modifier: Modifier = Modifier,
    onLoadFile: (Uri?) -> Unit = { _ -> },
    onSaveFile: (Uri?) -> Unit = { _ -> },
) {
    val fileLoader = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> onLoadFile(uri) }
    )

    val fileSaver = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(mimeType = "text/plain"),
        onResult = { uri -> onSaveFile(uri) }
    )

    TopAppBar(
        title = {
            Text(text = "ShumIDE")
        },
        modifier = modifier,
        actions = {
            IconButton(
                onClick = {
                    fileSaver.launch(input = "file.txt")
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_save_24),
                    contentDescription = "Save",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(
                onClick = {
                    fileLoader.launch(input = "*/*")
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_snippet_folder_24),
                    contentDescription = "Load",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewHomeUI() {
    ShumIDETheme {
        HomeUI()
    }
}