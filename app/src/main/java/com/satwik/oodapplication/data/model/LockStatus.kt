package com.satwik.oodapplication.data.model

import com.google.firebase.firestore.PropertyName

data class LockStatus(
    @get:PropertyName("date")
    @set:PropertyName("date")
    var date: String = "",

    @get:PropertyName("locked")
    @set:PropertyName("locked")
    var locked: Boolean = false,

    @get:PropertyName("breakfastLocked")
    @set:PropertyName("breakfastLocked")
    var breakfastLocked: Boolean = false,

    @get:PropertyName("lunchLocked")
    @set:PropertyName("lunchLocked")
    var lunchLocked: Boolean = false,

    @get:PropertyName("dinnerLocked")
    @set:PropertyName("dinnerLocked")
    var dinnerLocked: Boolean = false,

    @get:PropertyName("snackLocked")
    @set:PropertyName("snackLocked")
    var snackLocked: Boolean = false,

    @get:PropertyName("lockedBy")
    @set:PropertyName("lockedBy")
    var lockedBy: String? = null,

    @get:PropertyName("automationEnabled")
    @set:PropertyName("automationEnabled")
    var automationEnabled: Boolean = false
)
