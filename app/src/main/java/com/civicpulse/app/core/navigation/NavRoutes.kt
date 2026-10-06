package com.civicpulse.app.core.navigation

import kotlinx.serialization.Serializable

sealed interface NavRoute {
    @Serializable
    data object Login : NavRoute

    @Serializable
    data class ProviderHome(val uid: String, val name: String) : NavRoute

    @Serializable
    data class SupervisorHome(val uid: String, val name: String) : NavRoute

    @Serializable
    data class ClientHome(val uid: String, val name: String) : NavRoute
}
