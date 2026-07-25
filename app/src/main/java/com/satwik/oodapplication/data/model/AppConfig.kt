package com.satwik.oodapplication.data.model

data class AppConfig(
    val minVersionCode: Int = 1,
    val updateUrl: String = "",
    val updateMessage: String = "A new update is available. Please update the app to continue."
)
