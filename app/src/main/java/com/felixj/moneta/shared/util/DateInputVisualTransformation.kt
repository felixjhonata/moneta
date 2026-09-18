package com.felixj.moneta.shared.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Creates and remembers a [DateInputVisualTransformation].
 *
 * @param separator Separator character between date components (defaults to '/').
 * @param maskChar Placeholder character for unfilled slots (defaults to '_').
 */
@Composable
fun rememberDateInputVisualTransformation(
    separator: Char = '/',
    maskChar: Char = '_'
): DateInputVisualTransformation {
    return remember(separator, maskChar) {
        DateInputVisualTransformation(separator = separator, maskChar = maskChar)
    }
}

/**
 * A [VisualTransformation] that transforms raw digit date input (e.g. "01022026")
 * into a formatted date representation (e.g. "01/02/2026") with progressive masking
 * for unfilled digits (e.g. "__/__/____", "0_/__/____", "01/__/____").
 *
 * It provides a monotonic, bounds-safe [OffsetMapping] that:
 * - Automatically advances cursor over separators while typing.
 * - Prevents cursor glitches by mapping unfilled mask areas back to the end of user input.
 * - Supports seamless single-key backspacing past separators.
 * - Zero allocations on empty state and cached mappings for all common digit lengths (0..8).
 *
 * @param separator The character used to separate day, month, and year parts (default '/').
 * @param maskChar The character used for unfilled digit slots (default '_').
 */
class DateInputVisualTransformation(
    private val separator: Char = '/',
    private val maskChar: Char = '_'
) : VisualTransformation {

    private val emptyFormattedText: String = buildString(10) {
        append(maskChar).append(maskChar).append(separator)
        append(maskChar).append(maskChar).append(separator)
        append(maskChar).append(maskChar).append(maskChar).append(maskChar)
    }

    private val emptyAnnotatedString: AnnotatedString = AnnotatedString(emptyFormattedText)

    private val offsetMappings: Array<OffsetMapping> = Array(9) { length ->
        DateOffsetMapping(length)
    }

    private val emptyTransformedText: TransformedText = TransformedText(
        text = emptyAnnotatedString,
        offsetMapping = offsetMappings[0]
    )

    override fun filter(text: AnnotatedString): TransformedText {
        val rawText = text.text
        if (rawText.isEmpty()) {
            return emptyTransformedText
        }

        val trimmedLength = rawText.length.coerceAtMost(8)
        val chars = CharArray(10)
        var rawIdx = 0
        for (i in 0 until 10) {
            if (i == 2 || i == 5) {
                chars[i] = separator
            } else if (rawIdx < trimmedLength) {
                chars[i] = rawText[rawIdx++]
            } else {
                chars[i] = maskChar
            }
        }

        val mapping = if (rawText.length <= 8) {
            offsetMappings[rawText.length]
        } else {
            DateOffsetMapping(rawText.length)
        }

        return TransformedText(
            text = AnnotatedString(String(chars)),
            offsetMapping = mapping
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DateInputVisualTransformation) return false
        return separator == other.separator && maskChar == other.maskChar
    }

    override fun hashCode(): Int {
        return 31 * separator.hashCode() + maskChar.hashCode()
    }

    private class DateOffsetMapping(private val rawTextLength: Int) : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            val clamped = offset.coerceIn(0, rawTextLength)
            return when {
                clamped <= 1 -> clamped
                clamped <= 3 -> clamped + 1
                clamped <= 7 -> clamped + 2
                else -> 10
            }
        }

        override fun transformedToOriginal(offset: Int): Int {
            val clamped = offset.coerceIn(0, 10)
            val rawOffset = when {
                clamped <= 1 -> clamped
                clamped <= 3 -> 2
                clamped == 4 -> 3
                clamped <= 6 -> 4
                clamped == 7 -> 5
                clamped == 8 -> 6
                clamped == 9 -> 7
                else -> 8
            }
            return rawOffset.coerceAtMost(rawTextLength)
        }
    }
}
