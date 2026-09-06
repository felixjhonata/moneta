package com.felixj.moneta.shared.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

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

    data object Empty: UiText

    @Composable
    fun asString() = when (this) {
        is DynamicString -> value
        is StringResource -> stringResource(resourceId, *formatArgs)
        is Empty -> ""
    }
}
