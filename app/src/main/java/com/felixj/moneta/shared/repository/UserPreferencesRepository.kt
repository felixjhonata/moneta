package com.felixj.moneta.shared.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.felixj.moneta.settings.model.Currency
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext applicationContext: Context
) {
    private val prefs: SharedPreferences = applicationContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CURRENCY = "currency_key"
        private const val KEY_DARK_MODE = "dark_mode_key"
        private val DEFAULT_CURRENCY = Currency.IDR
    }

    val currencyFlow = callbackFlow {
        trySend(getCurrency())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_CURRENCY) {
                trySend(getCurrency())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun getCurrency(): Currency {
        val rawName = prefs.getString(KEY_CURRENCY, null) ?: return DEFAULT_CURRENCY
        return runCatching { Currency.valueOf(rawName) }.getOrDefault(DEFAULT_CURRENCY)
    }

    val darkModeFlow = callbackFlow {
        trySend(isDarkMode())

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_DARK_MODE) {
                trySend(isDarkMode())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun isDarkMode(): Boolean = prefs.getBoolean(KEY_DARK_MODE, false)

    fun setCurrency(currency: Currency) {
        prefs.edit {
            putString(KEY_CURRENCY, currency.name)
        }
    }

    fun setDarkMode(isDarkMode: Boolean) {
        prefs.edit {
            putBoolean(KEY_DARK_MODE, isDarkMode)
        }
    }
}