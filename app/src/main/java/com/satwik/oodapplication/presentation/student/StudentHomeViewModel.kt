package com.satwik.oodapplication.presentation.student

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satwik.oodapplication.MainActivity
import com.satwik.oodapplication.R
import com.satwik.oodapplication.data.model.AppNotification
import com.satwik.oodapplication.domain.repository.NotificationRepository
import com.satwik.oodapplication.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentHomeViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _notifications = MutableStateFlow<Resource<List<AppNotification>>>(Resource.Loading())
    val notifications: StateFlow<Resource<List<AppNotification>>> = _notifications

    private val sharedPrefs = context.getSharedPreferences("app_notifications", Context.MODE_PRIVATE)

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            notificationRepository.getAllNotifications().collect { resource ->
                _notifications.value = resource
                if (resource is Resource.Success) {
                    checkForNewPushNotifications(resource.data ?: emptyList())
                }
            }
        }
    }

    private fun checkForNewPushNotifications(list: List<AppNotification>) {
        val latestPush = list.filter { it.isPush }.maxByOrNull { it.timestamp }
        
        if (latestPush != null) {
            val lastSeenId = sharedPrefs.getString("last_seen_push_id", null)
            
            // Only trigger if it's a new notification and sent within the last hour
            val isRecent = System.currentTimeMillis() - latestPush.timestamp < 3600000 
            
            if (latestPush.id != lastSeenId && isRecent) {
                showLocalNotification(latestPush.title, latestPush.body)
                sharedPrefs.edit().putString("last_seen_push_id", latestPush.id).apply()
            }
        }
    }

    private fun showLocalNotification(title: String, body: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "admin_alerts")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
