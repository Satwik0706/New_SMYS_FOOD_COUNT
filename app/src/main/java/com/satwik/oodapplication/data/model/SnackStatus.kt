package com.satwik.oodapplication.data.model

import com.google.firebase.firestore.PropertyName

data class SnackStatus(
    @get:PropertyName("locked")
    @set:PropertyName("locked")
    var locked: Boolean = false,
    
    @get:PropertyName("lastUpdated")
    @set:PropertyName("lastUpdated")
    var lastUpdated: Long = System.currentTimeMillis(),
    
    @get:PropertyName("updatedBy")
    @set:PropertyName("updatedBy")
    var updatedBy: String? = null
)
