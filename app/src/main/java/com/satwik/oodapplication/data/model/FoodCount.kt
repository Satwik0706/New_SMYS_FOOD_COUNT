package com.satwik.oodapplication.data.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class FoodCount(
    @get:PropertyName("studentId")
    @set:PropertyName("studentId")
    var studentId: String = "",

    @get:PropertyName("date")
    @set:PropertyName("date")
    var date: String = "",

    // We use nullable internally to detect "Missing" data for the copy feature
    @get:PropertyName("breakfast")
    @set:PropertyName("breakfast")
    var breakfast: Boolean? = null,

    @get:PropertyName("lunch")
    @set:PropertyName("lunch")
    var lunch: Boolean? = null,

    @get:PropertyName("snack")
    @set:PropertyName("snack")
    var snack: Boolean? = null,

    @get:PropertyName("dinner")
    @set:PropertyName("dinner")
    var dinner: Boolean? = null,

    @get:PropertyName("lunchBox")
    @set:PropertyName("lunchBox")
    var lunchBox: Boolean? = null,

    // Rename to avoid 'is' prefix conflict with Firestore mapper
    @get:PropertyName("isLeave")
    @set:PropertyName("isLeave")
    var isLeave: Boolean? = null,

    @get:PropertyName("submittedAt")
    @set:PropertyName("submittedAt")
    var submittedAt: Long? = null,

    @get:PropertyName("lockedBy")
    @set:PropertyName("lockedBy")
    var lockedBy: String? = null
) {
    // Helper properties for the UI and logic
    @get:Exclude
    val isBreakfast: Boolean get() = breakfast ?: false
    @get:Exclude
    val isLunch: Boolean get() = lunch ?: false
    @get:Exclude
    val isSnack: Boolean get() = snack ?: false
    @get:Exclude
    val isDinner: Boolean get() = dinner ?: false
    @get:Exclude
    val isLunchBox: Boolean get() = lunchBox ?: false
    @get:Exclude
    val isOnLeave: Boolean get() = isLeave ?: false
}
