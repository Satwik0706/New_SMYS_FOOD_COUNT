package com.satwik.oodapplication.domain.repository

import com.satwik.oodapplication.data.model.AppNotification
import com.satwik.oodapplication.utils.Resource
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getAllNotifications(): Flow<Resource<List<AppNotification>>>
    suspend fun sendNotification(notification: AppNotification): Resource<Unit>
    suspend fun deleteNotification(notification: AppNotification): Resource<Unit>
}
