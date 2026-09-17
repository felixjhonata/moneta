package com.felixj.moneta.shared.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.felixj.moneta.settings.model.Currency
import com.felixj.moneta.settings.model.LocalAppCurrency
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Creates and remembers a [CurrencyAmountInputVisualTransformation] tied to the current
 * [LocalAppCurrency] or an explicitly passed [Currency].
 *
 * @param currency The currency to format with. Defaults to [LocalAppCurrency.current].
 * @param includePrefix Whether to include the currency symbol/prefix (e.g. "$ " or "Rp ").
 * @param prefix Optional custom prefix override. If null, the prefix is derived from [currency].
 * @param locale Optional custom locale override. If null, the locale is derived from [currency].
 */
@Composable
fun rememberCurrencyAmountInputVisualTransformation(
    currency: Currency = LocalAppCurrency.current,
    includePrefix: Boolean = true,
    prefix: String? = null,
    locale: Locale? = null
): CurrencyAmountInputVisualTransformation {
    return remember(currency, includePrefix, prefix, locale) {
        val resolvedPrefix = when {
            prefix != null -> prefix
            !includePrefix -> ""
            else -> when (currency) {
                Currency.IDR -> "Rp "
                Currency.USD -> "$ "
            }
        }
        val resolvedLocale = locale ?: when (currency) {
            Currency.IDR -> Locale.forLanguageTag("id-ID")
            Currency.USD -> Locale.US
        }
        CurrencyAmountInputVisualTransformation(
            currency = currency,
            prefix = resolvedPrefix,
            locale = resolvedLocale
        )
    }
}

/**
 * A [VisualTransformation] designed for numeric input fields that formats raw digit strings
 * into localized number format (e.g., 10000 -> 10,000 or 10.000) with an optional currency prefix.
 *
 * Key features:
 * - Locale-aware grouping separator (retrieved from [DecimalFormatSymbols.getInstance]).
 * - Supports [Currency] enum directly, or custom prefix and locale.
 * - Handles arbitrary digit lengths without [Long] overflow or precision loss.
 * - Monotonic, bounds-safe O(1) [OffsetMapping] with zero allocations during cursor queries.
 * - Displays empty text as identity mapping so input placeholders/hints remain visible.
 *
 * @param currency Optional [Currency] to derive default prefix and locale from.
 * @param prefix Optional prefix string (e.g., "$", "Rp", "$ ", or ""). If null and [currency] is
 * provided, uses the currency's standard prefix. If neither is provided, defaults to empty.
 * @param locale Optional [Locale] to use for formatting. If null, derived from [currency] or [Locale.getDefault].
 */
class CurrencyAmountInputVisualTransformation(
    currency: Currency? = null,
    prefix: String? = null,
    locale: Locale? = null
) : VisualTransformation {

    private val actualLocale: Locale = locale ?: when (currency) {
        Currency.IDR -> Locale.forLanguageTag("id-ID")
        Currency.USD -> Locale.US
        null -> Locale.getDefault()
    }

    private val groupingSeparator: Char =
        DecimalFormatSymbols.getInstance(actualLocale).groupingSeparator

    private val formattedPrefix: String = when {
        prefix != null -> if (prefix.isEmpty() || prefix.endsWith(" ")) prefix else "$prefix "
        currency != null -> when (currency) {
            Currency.IDR -> "Rp "
            Currency.USD -> "$ "
        }
        else -> ""
    }

    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        // Format raw string with thousands grouping separators from the right
        val originalLength = originalText.length
        val formattedNumber = buildString {
            for (i in 0 until originalLength) {
                append(originalText[i])
                val digitsRemaining = originalLength - 1 - i
                if (digitsRemaining > 0 && digitsRemaining % 3 == 0) {
                    append(groupingSeparator)
                }
            }
        }

        val formattedText = "$formattedPrefix$formattedNumber"
        val prefixLength = formattedPrefix.length
        val transformedLength = formattedText.length

        // Precompute originalToTransformed mapping table (size = originalLength + 1)
        val originalToTransformed = IntArray(originalLength + 1)
        val totalSeparators = (originalLength - 1) / 3
        for (rawOffset in 0..originalLength) {
            val digitsRemaining = originalLength - rawOffset
            val separatorsToRight = if (digitsRemaining > 0) (digitsRemaining - 1) / 3 else 0
            val separatorsBefore = totalSeparators - separatorsToRight
            originalToTransformed[rawOffset] = (prefixLength + rawOffset + separatorsBefore)
                .coerceIn(0, transformedLength)
        }

        // Precompute transformedToOriginal mapping table (size = transformedLength + 1)
        val transformedToOriginal = IntArray(transformedLength + 1)
        var rawCount = 0
        for (transOffset in 0..transformedLength) {
            if (transOffset <= prefixLength) {
                transformedToOriginal[transOffset] = 0
            } else {
                val numberIndex = transOffset - prefixLength - 1
                if (numberIndex in formattedNumber.indices && formattedNumber[numberIndex] != groupingSeparator) {
                    rawCount++
                }
                transformedToOriginal[transOffset] = rawCount.coerceIn(0, originalLength)
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return originalToTransformed[offset.coerceIn(0, originalLength)]
            }

            override fun transformedToOriginal(offset: Int): Int {
                return transformedToOriginal[offset.coerceIn(0, transformedLength)]
            }
        }

        return TransformedText(
            text = AnnotatedString(formattedText),
            offsetMapping = offsetMapping
        )
    }
}
