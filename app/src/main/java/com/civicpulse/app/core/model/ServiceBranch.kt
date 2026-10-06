package com.civicpulse.app.core.model

enum class ServiceBranch(
    val title: String,
    val subtitle: String,
    val code: String
) {
    SECURITY(
        title = "Security Pulse",
        subtitle = "Manned Guarding & Security Ops",
        code = "security"
    ),
    CLEAN(
        title = "Clean Pulse",
        subtitle = "Housekeeping & Facility Hygiene",
        code = "clean"
    ),
    FACILITY(
        title = "Facility Pulse",
        subtitle = "Integrated Facility Management",
        code = "facility"
    );

    companion object {
        fun fromCode(code: String): ServiceBranch {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: SECURITY
        }
    }
}
