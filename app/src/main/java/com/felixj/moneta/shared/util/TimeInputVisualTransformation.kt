package com.felixj.moneta.shared.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Creates and remembers a [TimeInputVisualTransformation].
 *
 * @param separator Separator character between hour and minute components (defaults to ':').
 * @param maskChar Placeholder character for unfilled slots (defaults to '_').
 */
@Composable
fun rememberTimeInputVisualTransformation(
    separator: Char = ':',
    maskChar: Char = '_'
): TimeInputVisualTransformation {
    return remember(separator, maskChar) {
        TimeInputVisualTransformation(separator = separator, maskChar = maskChar)
    }
}

/**
 * A [VisualTransformation] that transforms raw digit time input (e.g. "1430")
 * into a formatted time representation (e.g. "14:30") with progressive masking
 * for unfilled digits (e.g. "__:__", "1_:__", "14:__", "14:3_").
 *
 * It provides a monotonic, bounds-safe [OffsetMapping] that:
 * - Automatically advances cursor over the separator ':' while typing.
 * - Prevents cursor glitches by mapping unfilled mask areas back to the end of user input.
 * - Supports seamless single-key backspacing past the separator.
 * - Zero allocations on empty state and cached mappings for all common digit lengths (0..4).
 *
 * @param separator The character used to separate hour and minute parts (default ':').
 * @param maskChar The character used for unfilled digit slots (default '_').
 */
class TimeInputVisualTransformation(
    private val separator: Char = ':',
    private val maskChar: Char = '_'
) : VisualTransformation {

    private val emptyFormattedText: String = buildString(5) {
        append(maskChar).append(maskChar).append(separator).append(maskChar).append(maskChar)
    }

    private val emptyAnnotatedString: AnnotatedString = AnnotatedString(emptyFormattedText)

    private val offsetMappings: Array<OffsetMapping> = Array(5) { length ->
        TimeOffsetMapping(length)
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

        val trimmedLength = rawText.length.coerceAtMost(4)
        val chars = CharArray(5)
        var rawIdx = 0
        for (i in 0 until 5) {
            if (i == 2) {
                chars[i] = separator
            } else if (rawIdx < trimmedLength) {
                chars[i] = rawText[rawIdx++]
            } else {
                chars[i] = maskChar
            }
        }

        val mapping = if (rawText.length <= 4) {
            offsetMappings[rawText.length]
        } else {
            TimeOffsetMapping(rawText.length)
        }

        return TransformedText(
            text = AnnotatedString(String(chars)),
            offsetMapping = mapping
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TimeInputVisualTransformation) return false
        return separator == other.separator && maskChar == other.maskChar
    }

    override fun hashCode(): Int {
        return 31 * separator.hashCode() + maskChar.hashCode()
    }

    private class TimeOffsetMapping(private val rawTextLength: Int) : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            val clamped = offset.coerceIn(0, rawTextLength)
            return when {
                clamped <= 1 -> clamped
                clamped <= 3 -> clamped + 1
                else -> 5
            }
        }

        override fun transformedToOriginal(offset: Int): Int {
            val clamped = offset.coerceIn(0, 5)
            val rawOffset = when {
                clamped <= 1 -> clamped
                clamped <= 3 -> 2
                clamped == 4 -> 3
                else -> 4
            }
            return rawOffset.coerceAtMost(rawTextLength)
        }
    }
}
