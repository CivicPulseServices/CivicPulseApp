package com.civicpulse.app.core.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val uid: String = "",
    val name: String = "",
    val role: String = "", // "provider" | "supervisor" | "client"
    val phone: String = "",
    val email: String = "",
    val accountIds: List<String> = emptyList(),
    val selectedAccountId: String = ""
) {
    companion object {
        const val ROLE_PROVIDER = "provider"
        const val ROLE_SUPERVISOR = "supervisor"
        const val ROLE_CLIENT = "client"
    }

    val isProvider: Boolean get() = role.lowercase() == ROLE_PROVIDER
    val isSupervisor: Boolean get() = role.lowercase() == ROLE_SUPERVISOR
    val isClient: Boolean get() = role.lowercase() == ROLE_CLIENT
}
