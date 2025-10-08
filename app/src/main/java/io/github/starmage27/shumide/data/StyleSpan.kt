package io.github.starmage27.shumide.data

import androidx.compose.ui.text.SpanStyle

data class StyleSpan(
    val style: SpanStyle,
    val start: Int,
    val end: Int,
)