package com.satwik.oodapplication.data.model

import com.google.firebase.firestore.PropertyName

data class Attendance(
    val studentId: String = "",
    val studentName: String = "",
    val date: String = "",
    @get:PropertyName("isPresent")
    @PropertyName("isPresent")
    val isPresent: Boolean = true,
    val batch: String = ""
)
