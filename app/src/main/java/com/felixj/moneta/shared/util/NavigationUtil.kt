package com.felixj.moneta.shared.util

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.felixj.moneta.shared.model.MonetaRoute

private fun NavBackStack<NavKey>.navigateSingleTop(destination: MonetaRoute) {
    if (this.lastOrNull() == destination) return

    this.remove(destination)
    this.add(destination)
}

fun NavBackStack<NavKey>.navigateTo(destination: MonetaRoute) {
    when (destination) {
        MonetaRoute.Dashboard -> navigateSingleTop(MonetaRoute.Dashboard)
        MonetaRoute.History -> navigateSingleTop(MonetaRoute.History)
        MonetaRoute.Settings -> navigateSingleTop(MonetaRoute.Settings)
    }
}
