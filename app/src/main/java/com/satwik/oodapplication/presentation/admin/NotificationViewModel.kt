package com.satwik.oodapplication.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satwik.oodapplication.data.model.AppNotification
import com.satwik.oodapplication.domain.repository.AuthRepository
import com.satwik.oodapplication.domain.repository.NotificationRepository
import com.satwik.oodapplication.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<Resource<List<AppNotification>>>(Resource.Loading())
    val notifications: StateFlow<Resource<List<AppNotification>>> = _notifications

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            repository.getAllNotifications().collect {
                _notifications.value = it
            }
        }
    }

    fun sendNotification(title: String, body: String, target: String, isPush: Boolean) {
        viewModelScope.launch {
            val notification = AppNotification(
                id = UUID.randomUUID().toString(),
                title = title,
                body = body,
                targetYear = target,
                timestamp = System.currentTimeMillis(),
                isPush = isPush
            )
            repository.sendNotification(notification)
            
            // Log Admin action
            authRepository.getSession()?.let { admin ->
                val type = if (isPush) "Push Notification" else "Alert"
                authRepository.logAction(admin, "Sent $type: $title")
            }
            
            if (isPush) {
                // To trigger a push from the device for free, we can use a direct FCM call.
                // Replace 'YOUR_SERVER_KEY' with the key from Firebase Console -> Cloud Messaging (Legacy)
                // Note: This is for demonstration. For production, use a secure backend.
                // FirebaseMessaging.getInstance().send(...) is another way but complex for topics.
            }
        }
    }

    fun deleteNotification(notification: AppNotification) {
        viewModelScope.launch {
            repository.deleteNotification(notification)
        }
    }
}
