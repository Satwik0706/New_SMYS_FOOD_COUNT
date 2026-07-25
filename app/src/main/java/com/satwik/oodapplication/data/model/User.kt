package com.satwik.oodapplication.data.model

import com.google.firebase.firestore.PropertyName

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "",
    val year: String? = null,
    val rollNumber: String? = null,
    val adminId: String? = null,
    val password: String? = null, // For internal login verification
    val breakfastPref: Boolean = false,
    val lunchPref: Boolean = false,
    val snackPref: Boolean = false,
    val dinnerPref: Boolean = false,
    @get:PropertyName("isLeave")
    @PropertyName("isLeave")
    val isLeave: Boolean = false,
    @get:PropertyName("noFoodPref")
    @PropertyName("noFoodPref")
    val noFoodPref: Boolean = false,
    val appVersion: String? = null
)
