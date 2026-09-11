package com.felixj.moneta.settings.model

import androidx.annotation.StringRes
import com.felixj.moneta.R

enum class Currency(@StringRes val label: Int, @StringRes val symbol: Int) {
    USD(R.string.usd_currency_label, R.string.dollar_value),
    IDR(R.string.idr_currency_label, R.string.rp_value)
}