package starmage27.shumide.data

import androidx.compose.ui.text.SpanStyle

data class StyleSpan(
    val style: SpanStyle,
    val start: ULong,
    val end: ULong,
)