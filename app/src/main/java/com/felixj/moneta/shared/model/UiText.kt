package com.felixj.moneta.shared.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.felixj.moneta.settings.model.Currency
import com.felixj.moneta.settings.model.LocalAppCurrency
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

sealed interface UiText {
    data class DynamicString(val value: String) : UiText

    class StringResource(
        @StringRes val resourceId: Int,
        vararg val formatArgs: Any
    ) : UiText {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as StringResource

            if (resourceId != other.resourceId) return false
            if (!formatArgs.contentEquals(other.formatArgs)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = resourceId
            result = 31 * result + formatArgs.contentHashCode()
            return result
        }
    }

    data class CurrencyAmount(
        val amount: Long,
        val prefix: String = ""
    ) : UiText

    data object Empty: UiText

    @Composable
    fun asString(): String = when (this) {
        is DynamicString -> value
        is StringResource -> stringResource(resourceId, *formatArgs)
        is CurrencyAmount -> {
            val currentCurrency = LocalAppCurrency.current
            val locale = when (currentCurrency) {
                Currency.IDR -> Locale.forLanguageTag("id-ID")
                Currency.USD -> Locale.US
            }
            val formattedNumber = NumberFormat.getNumberInstance(locale).format(abs(amount))
            val formattedCurrency = stringResource(currentCurrency.symbol, formattedNumber)
            "$prefix$formattedCurrency"
        }
        is Empty -> ""
    }
}
