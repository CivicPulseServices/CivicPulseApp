package com.civicpulse.app.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Agent(
    val agentId: String = "",
    val name: String = "",
    val phone: String = "",
    val serviceType: String = "", // "security" | "housekeeping"
    val clientId: String = "",
    val supervisorId: String = ""
) {
    companion object {
        const val SERVICE_SECURITY = "security"
        const val SERVICE_HOUSEKEEPING = "housekeeping"
    }
}
