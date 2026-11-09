package com.mskd.flux.navigation.domain

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

fun NavBackStack<NavKey>.popScreen() {
    if (this.size > 1) this.removeLastOrNull()
}

/** Pops back to the nearest tab screen (Catalog, Search, Settings), e.g. straight home from the player. */
fun NavBackStack<NavKey>.popToTab() {
    while (this.size > 1) {
        val route = this.lastOrNull() as? Route
        if (route is Route.Catalog || route is Route.Search || route is Route.Settings) return
        this.removeLastOrNull()
    }
}
