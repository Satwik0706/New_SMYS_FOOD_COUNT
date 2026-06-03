package com.satwik.oodapplication.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.satwik.oodapplication.data.model.AuditLog
import com.satwik.oodapplication.data.model.FoodCount
import com.satwik.oodapplication.data.model.LockStatus
import com.satwik.oodapplication.data.model.User
import com.satwik.oodapplication.domain.repository.FoodCountRepository
import com.satwik.oodapplication.utils.Constants
import com.satwik.oodapplication.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseFoodCountRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FoodCountRepository {

    override fun getStudentFoodCount(studentId: String, date: String): Flow<Resource<FoodCount>> = callbackFlow {
        val dailyRef = firestore.collection(Constants.COLLECTION_FOODCOUNTS).document("${studentId}_$date")
        val userRef = firestore.collection(Constants.COLLECTION_USERS).document(studentId)
        
        var dailySubscription: com.google.firebase.firestore.ListenerRegistration? = null
        
        // Listen to User preferences first for real-time fallback
        val userSubscription = userRef.addSnapshotListener { userSnapshot, userError ->
            if (userError != null) return@addSnapshotListener
            val user = userSnapshot?.toObject(User::class.java)
            
            // Now listen to Daily record
            if (dailySubscription == null) {
                dailySubscription = dailyRef.addSnapshotListener { dailySnapshot, dailyError ->
                    if (dailyError != null) return@addSnapshotListener
                    
                    if (dailySnapshot != null && dailySnapshot.exists()) {
                        trySend(Resource.Success(dailySnapshot.toObject(FoodCount::class.java)!!))
                    } else {
                        // Fallback to User preferences (which are also real-time now)
                        val count = FoodCount(
                            studentId = studentId,
                            date = date,
                            breakfast = user?.breakfastPref ?: false,
                            lunch = user?.lunchPref ?: false,
                            snack = user?.snackPref ?: false,
                            dinner = user?.dinnerPref ?: false,
                            lunchBox = false,
                            isLeave = user?.isLeave ?: false
                        )
                        trySend(Resource.Success(count))
                    }
                }
            }
        }
        
        awaitClose { 
            userSubscription.remove()
            dailySubscription?.remove()
        }
    }

    override suspend fun submitFoodCount(foodCount: FoodCount): Resource<Unit> {
        return try {
            // 1. Update daily record
            firestore.collection(Constants.COLLECTION_FOODCOUNTS)
                .document("${foodCount.studentId}_${foodCount.date}")
                .set(foodCount)
                .await()
                
            // 2. Update sticky preferences in User document
            // This ensures "On Leave" and "No Food" state persists across app restarts
            val updates = mapOf(
                "breakfastPref" to foodCount.breakfast,
                "lunchPref" to foodCount.lunch,
                "snackPref" to foodCount.snack,
                "dinnerPref" to foodCount.dinner,
                "isLeave" to foodCount.isLeave
            )
            firestore.collection(Constants.COLLECTION_USERS)
                .document(foodCount.studentId)
                .update(updates)
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Submission failed")
        }
    }

    override fun getLockStatus(date: String): Flow<Resource<LockStatus>> = callbackFlow {
        val subscription = firestore.collection(Constants.COLLECTION_LOCK_STATUS)
            .document(date)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error"))
                    return@addSnapshotListener
                }
                val status = snapshot?.toObject(LockStatus::class.java) ?: LockStatus(date = date)
                trySend(Resource.Success(status))
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun updateLockStatus(lockStatus: LockStatus): Resource<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_LOCK_STATUS)
                .document(lockStatus.date)
                .set(lockStatus)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Update failed")
        }
    }

    override fun getAllFoodCounts(date: String): Flow<Resource<List<FoodCount>>> = callbackFlow {
        val subscription = firestore.collection(Constants.COLLECTION_FOODCOUNTS)
            .whereEqualTo("date", date)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error"))
                    return@addSnapshotListener
                }
                val counts = snapshot?.toObjects(FoodCount::class.java) ?: emptyList()
                trySend(Resource.Success(counts))
            }
        awaitClose { subscription.remove() }
    }

    override fun getAuditLogs(): Flow<List<AuditLog>> = callbackFlow {
        val subscription = firestore.collection(Constants.COLLECTION_LOGS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.toObjects(AuditLog::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun clearOldLogs(beforeTimestamp: Long) {
        val snapshots = firestore.collection(Constants.COLLECTION_LOGS)
            .whereLessThan("timestamp", beforeTimestamp)
            .get()
            .await()
        
        val batch = firestore.batch()
        snapshots.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        batch.commit().await()
    }
}
