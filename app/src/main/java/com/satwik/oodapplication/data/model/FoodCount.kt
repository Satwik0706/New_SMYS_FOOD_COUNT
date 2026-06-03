package com.satwik.oodapplication.data.model

import com.google.firebase.firestore.PropertyName

data class FoodCount(
    val studentId: String = "",
    val date: String = "",
    val breakfast: Boolean = false,
    val lunch: Boolean = false,
    val snack: Boolean = false,
    val dinner: Boolean = false,
    val lunchBox: Boolean = false,
    @get:PropertyName("isLeave")
    @PropertyName("isLeave")
    val isLeave: Boolean = false,
    val submittedAt: Long? = null,
    val lockedBy: String? = null
)
