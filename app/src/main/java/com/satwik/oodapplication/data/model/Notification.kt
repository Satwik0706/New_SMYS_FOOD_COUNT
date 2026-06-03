package com.satwik.oodapplication.data.model

import com.google.firebase.firestore.PropertyName

data class AppNotification(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val priority: String = "Normal",
    val timestamp: Long = System.currentTimeMillis(),
    val sentBy: String = "",
    val targetYear: String = "All",
    @get:PropertyName("isPush")
    @PropertyName("isPush")
    val isPush: Boolean = false
)
