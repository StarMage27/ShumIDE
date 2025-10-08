package io.github.starmage27.shumide

import androidx.compose.ui.text.SpanStyle
import io.github.starmage27.shumide.data.StyleSpan
import uniffi.treesitterbridge.DiffRange
import uniffi.treesitterbridge.Highlight

object Utils {
    fun List<Highlight>.toStyleSpans(styleProvider: (kind: UShort) -> SpanStyle): List<StyleSpan> {
        return this.map { highlight ->
            val styleSpan = StyleSpan(
                style = styleProvider(highlight.kind),
                start = highlight.start,
                end = highlight.end,
            )
            styleSpan
        }
    }

    fun <T> getDiffRange(old: T, new: T): DiffRange?
    where T : CharSequence
    {
        val oldLen = old.length
        val newLen = new.length
        val minLen = minOf(oldLen, newLen)

        // Find first differing index
        var start = 0
        while (start < minLen && old[start] == new[start]) {
            start++
        }

        // No difference
        if (oldLen == newLen && start == oldLen) {
            return null
            //return DiffRange(start, start, start)
        }

        // Find last differing index
        var oldEnd = oldLen
        var newEnd = newLen
        while (oldEnd > start && newEnd > start && old[oldEnd - 1] == new[newEnd - 1]) {
            oldEnd--
            newEnd--
        }

        return DiffRange(start, oldEnd, newEnd)
    }
}