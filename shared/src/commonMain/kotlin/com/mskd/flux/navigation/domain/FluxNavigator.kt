package com.mskd.flux.navigation.domain

class FluxNavigator(
    val navigate: (Route) -> Unit,
    val onBack: () -> Unit,
    /** Back to the tab screen under the current one, skipping the detail screens in between. */
    val backToTab: () -> Unit,
    val clearAndNavigate: (Route) -> Unit,
    /** Replaces the current destination: back leads to the one before it. */
    val replace: (Route) -> Unit,
)