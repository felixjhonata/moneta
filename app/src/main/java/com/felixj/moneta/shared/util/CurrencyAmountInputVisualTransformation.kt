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
    val currency: Currency? = null,
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

    private val decimalSeparator: Char =
        DecimalFormatSymbols.getInstance(actualLocale).decimalSeparator

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

        val (formattedNumber, realDigitPositions) = buildFormattedNumber(originalText)
        val formattedText = "$formattedPrefix$formattedNumber"
        val prefixLength = formattedPrefix.length
        val transformedLength = formattedText.length
        val originalLength = originalText.length

        // originalToTransformed: position of the k-th real (typed) digit, or end after the last one
        val originalToTransformed = IntArray(originalLength + 1)
        for (k in 0 until originalLength) {
            originalToTransformed[k] = prefixLength + realDigitPositions[k]
        }
        originalToTransformed[originalLength] = transformedLength

        // transformedToOriginal: number of real digits strictly before the position
        val transformedToOriginal = IntArray(transformedLength + 1)
        var realConsumed = 0
        for (pos in 0..transformedLength) {
            while (realConsumed < originalLength &&
                prefixLength + realDigitPositions[realConsumed] < pos
            ) {
                realConsumed++
            }
            transformedToOriginal[pos] = realConsumed
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

    /**
     * Builds the formatted number string and the position of each typed digit within it.
     *
     * For [Currency.USD], amounts are stored in cents: the last 2 typed digits become the
     * decimal part (e.g. "2005" -> "20.05"), and short inputs are zero-padded (e.g. "5" -> "0.05").
     * Positions of typed digits are tracked so offset mapping can distinguish them from
     * zero-padding digits.
     */
    private fun buildFormattedNumber(raw: String): Pair<String, IntArray> {
        val out = StringBuilder()
        val realDigitPositions = IntArray(raw.length)
        val n = raw.length

        when (currency) {
            Currency.USD -> {
                val intLen = (n - 2).coerceAtLeast(0)
                if (intLen == 0) {
                    out.append('0')
                } else {
                    for (i in 0 until intLen) {
                        val digitsRemaining = intLen - 1 - i
                        realDigitPositions[i] = out.length
                        out.append(raw[i])
                        if (digitsRemaining > 0 && digitsRemaining % 3 == 0) {
                            out.append(groupingSeparator)
                        }
                    }
                }
                out.append(decimalSeparator)
                val centsStart = (n - 2).coerceAtLeast(0)
                repeat(2 - (n - centsStart)) { out.append('0') }
                for (i in centsStart until n) {
                    realDigitPositions[i] = out.length
                    out.append(raw[i])
                }
            }
            Currency.IDR, null -> {
                for (i in 0 until n) {
                    val digitsRemaining = n - 1 - i
                    realDigitPositions[i] = out.length
                    out.append(raw[i])
                    if (digitsRemaining > 0 && digitsRemaining % 3 == 0) {
                        out.append(groupingSeparator)
                    }
                }
            }
        }

        return out.toString() to realDigitPositions
    }
}
