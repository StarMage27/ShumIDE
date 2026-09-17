package starmage27.shumide

import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import starmage27.shumide.data.StyleSpan

class OutputTransformationHighlight(
    private val spanStylesProvider: () -> List<StyleSpan>,
): OutputTransformation {
    override fun TextFieldBuffer.transformOutput() {
        val styles = spanStylesProvider()
        styles.forEach { styleSpan ->
            addStyle(
                spanStyle = styleSpan.style,
                start = styleSpan.start.toInt(),
                end = styleSpan.end.toInt()
            )
        }
    }
}

