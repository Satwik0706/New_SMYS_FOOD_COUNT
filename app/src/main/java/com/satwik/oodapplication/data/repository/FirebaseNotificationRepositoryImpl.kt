package com.satwik.oodapplication.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.satwik.oodapplication.data.model.AppNotification
import com.satwik.oodapplication.domain.repository.NotificationRepository
import com.satwik.oodapplication.utils.Constants
import com.satwik.oodapplication.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseNotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    override fun getAllNotifications(): Flow<Resource<List<AppNotification>>> = callbackFlow {
        val subscription = firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error"))
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(AppNotification::class.java) ?: emptyList()
                trySend(Resource.Success(list))
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun sendNotification(notification: AppNotification): Resource<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
                .document(notification.id)
                .set(notification)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to send notification")
        }
    }

    override suspend fun deleteNotification(notification: AppNotification): Resource<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_NOTIFICATIONS)
                .document(notification.id)
                .delete()
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete notification")
        }
    }
}
