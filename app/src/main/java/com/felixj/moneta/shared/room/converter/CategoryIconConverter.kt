package com.felixj.moneta.shared.room.converter

import androidx.annotation.DrawableRes
import androidx.room3.ColumnTypeConverter
import com.felixj.moneta.R

object CategoryIconConverter {
    @ColumnTypeConverter
    fun toDrawableRes(code: String): Int = when (code) {
        "baseline_lightbulb_24" -> R.drawable.baseline_lightbulb_24
        "baseline_fastfood_24" -> R.drawable.baseline_fastfood_24
        "baseline_directions_bus_24" -> R.drawable.baseline_directions_bus_24
        "baseline_account_balance_wallet_24" -> R.drawable.baseline_account_balance_wallet_24
        "baseline_add_24" -> R.drawable.baseline_add_24
        "baseline_history_24" -> R.drawable.baseline_history_24
        "baseline_home_filled_24" -> R.drawable.baseline_home_filled_24
        "baseline_settings_24" -> R.drawable.baseline_settings_24
        else -> R.drawable.baseline_lightbulb_24
    }

    @ColumnTypeConverter
    fun toCode(@DrawableRes resId: Int): String = when (resId) {
        R.drawable.baseline_lightbulb_24 -> "baseline_lightbulb_24"
        R.drawable.baseline_fastfood_24 -> "baseline_fastfood_24"
        R.drawable.baseline_directions_bus_24 -> "baseline_directions_bus_24"
        R.drawable.baseline_account_balance_wallet_24 -> "baseline_account_balance_wallet_24"
        R.drawable.baseline_add_24 -> "baseline_add_24"
        R.drawable.baseline_history_24 -> "baseline_history_24"
        R.drawable.baseline_home_filled_24 -> "baseline_home_filled_24"
        R.drawable.baseline_settings_24 -> "baseline_settings_24"
        else -> "baseline_lightbulb_24"
    }
}
