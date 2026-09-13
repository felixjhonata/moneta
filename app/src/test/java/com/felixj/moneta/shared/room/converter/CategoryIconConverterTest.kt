package com.felixj.moneta.shared.room.converter

import com.felixj.moneta.R
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryIconConverterTest {

    @Test
    fun toDrawableRes_convertsKnownCodesToCorrectResourceIds() {
        assertEquals(R.drawable.baseline_lightbulb_24, CategoryIconConverter.toDrawableRes("baseline_lightbulb_24"))
        assertEquals(R.drawable.baseline_fastfood_24, CategoryIconConverter.toDrawableRes("baseline_fastfood_24"))
        assertEquals(R.drawable.baseline_directions_bus_24, CategoryIconConverter.toDrawableRes("baseline_directions_bus_24"))
        assertEquals(R.drawable.baseline_account_balance_wallet_24, CategoryIconConverter.toDrawableRes("baseline_account_balance_wallet_24"))
        assertEquals(R.drawable.baseline_add_24, CategoryIconConverter.toDrawableRes("baseline_add_24"))
        assertEquals(R.drawable.baseline_history_24, CategoryIconConverter.toDrawableRes("baseline_history_24"))
        assertEquals(R.drawable.baseline_home_filled_24, CategoryIconConverter.toDrawableRes("baseline_home_filled_24"))
        assertEquals(R.drawable.baseline_settings_24, CategoryIconConverter.toDrawableRes("baseline_settings_24"))
    }

    @Test
    fun toDrawableRes_unknownCode_returnsFallback() {
        assertEquals(R.drawable.baseline_lightbulb_24, CategoryIconConverter.toDrawableRes("unknown_code"))
        assertEquals(R.drawable.baseline_lightbulb_24, CategoryIconConverter.toDrawableRes(""))
    }

    @Test
    fun toCode_convertsKnownCategoryIconsToCorrectCodes() {
        assertEquals("baseline_lightbulb_24", CategoryIconConverter.toCode(R.drawable.baseline_lightbulb_24))
        assertEquals("baseline_fastfood_24", CategoryIconConverter.toCode(R.drawable.baseline_fastfood_24))
        assertEquals("baseline_directions_bus_24", CategoryIconConverter.toCode(R.drawable.baseline_directions_bus_24))
        assertEquals("baseline_account_balance_wallet_24", CategoryIconConverter.toCode(R.drawable.baseline_account_balance_wallet_24))
        assertEquals("baseline_add_24", CategoryIconConverter.toCode(R.drawable.baseline_add_24))
        assertEquals("baseline_history_24", CategoryIconConverter.toCode(R.drawable.baseline_history_24))
        assertEquals("baseline_home_filled_24", CategoryIconConverter.toCode(R.drawable.baseline_home_filled_24))
        assertEquals("baseline_settings_24", CategoryIconConverter.toCode(R.drawable.baseline_settings_24))
    }

    @Test
    fun toCode_unknownResId_returnsFallback() {
        assertEquals("baseline_lightbulb_24", CategoryIconConverter.toCode(-999))
    }

    @Test
    fun roundTrip_isBiDirectional() {
        val codes = listOf(
            "baseline_lightbulb_24",
            "baseline_fastfood_24",
            "baseline_directions_bus_24",
            "baseline_account_balance_wallet_24",
            "baseline_add_24",
            "baseline_history_24",
            "baseline_home_filled_24",
            "baseline_settings_24"
        )
        for (code in codes) {
            val resId = CategoryIconConverter.toDrawableRes(code)
            val roundTripCode = CategoryIconConverter.toCode(resId)
            assertEquals(code, roundTripCode)
        }
    }
}
