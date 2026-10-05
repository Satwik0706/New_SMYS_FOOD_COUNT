package com.satwik.oodapplication.data.model

data class AdminWhatsAppConfig(
    val numbers: List<String> = emptyList(),
    val strategy: String = "SEQUENTIAL",
    val currentIndex: Int = 0
)
