package com.civicpulse.app.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Attendance(
    val id: String = "", // e.g. "agentId_yyyy-MM-dd"
    val agentId: String = "",
    val clientId: String = "",
    val supervisorId: String = "",
    val date: String = "", // yyyy-MM-dd
    val status: String = "", // "present" | "absent"
    val markedBy: String = ""
) {
    companion object {
        const val STATUS_PRESENT = "present"
        const val STATUS_ABSENT = "absent"

        fun generateId(agentId: String, date: String): String {
            return "${agentId}_$date"
        }
    }
}
