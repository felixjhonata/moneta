package com.felixj.moneta.shared.model

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface MonetaRoute: NavKey {
    @Serializable
    data object Dashboard: MonetaRoute

    @Serializable
    data object History: MonetaRoute

    @Serializable
    data object Settings: MonetaRoute

    @Serializable
    data object AddActivity: MonetaRoute
}