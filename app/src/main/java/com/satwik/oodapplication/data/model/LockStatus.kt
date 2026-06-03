package com.satwik.oodapplication.data.model

data class LockStatus(
    val date: String = "",
    val locked: Boolean = false, // Master lock (locks everything)
    val breakfastLocked: Boolean = false,
    val lunchLocked: Boolean = false,
    val snackLocked: Boolean = false,
    val dinnerLocked: Boolean = false,
    val lockedBy: String? = null
)
