package com.satwik.oodapplication.data.model

data class AuditLog(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val action: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
