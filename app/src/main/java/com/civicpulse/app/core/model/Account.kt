package com.civicpulse.app.core.model

data class Account(
    val id: String = "",
    val name: String = "",
    val memberIds: List<String> = emptyList()
)
