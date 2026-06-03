package com.satwik.oodapplication.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.satwik.oodapplication.data.model.Attendance
import com.satwik.oodapplication.domain.repository.AttendanceRepository
import com.satwik.oodapplication.utils.Constants
import com.satwik.oodapplication.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAttendanceRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AttendanceRepository {

    override fun getAttendanceForDate(date: String): Flow<Resource<List<Attendance>>> = callbackFlow {
        val subscription = firestore.collection(Constants.COLLECTION_ATTENDANCE)
            .whereEqualTo("date", date)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error"))
                    return@addSnapshotListener
                }
                val list = snapshot?.toObjects(Attendance::class.java) ?: emptyList()
                trySend(Resource.Success(list))
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun saveAttendance(attendanceList: List<Attendance>): Resource<Unit> {
        return try {
            val batch = firestore.batch()
            attendanceList.forEach { attendance ->
                val docRef = firestore.collection(Constants.COLLECTION_ATTENDANCE)
                    .document("${attendance.studentId}_${attendance.date}")
                batch.set(docRef, attendance)
            }
            batch.commit().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to save attendance")
        }
    }

    override suspend fun clearOldAttendance(beforeDate: String): Resource<Unit> {
        return try {
            val snapshots = firestore.collection(Constants.COLLECTION_ATTENDANCE)
                .whereLessThan("date", beforeDate)
                .get()
                .await()
            
            val batch = firestore.batch()
            snapshots.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to clear old attendance")
        }
    }
}
