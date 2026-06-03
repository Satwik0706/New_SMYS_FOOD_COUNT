package com.satwik.oodapplication.domain.repository

import com.satwik.oodapplication.data.model.AuditLog
import com.satwik.oodapplication.data.model.FoodCount
import com.satwik.oodapplication.data.model.LockStatus
import com.satwik.oodapplication.utils.Resource
import kotlinx.coroutines.flow.Flow

interface FoodCountRepository {
    fun getStudentFoodCount(studentId: String, date: String): Flow<Resource<FoodCount>>
    suspend fun submitFoodCount(foodCount: FoodCount): Resource<Unit>
    fun getLockStatus(date: String): Flow<Resource<LockStatus>>
    suspend fun updateLockStatus(lockStatus: LockStatus): Resource<Unit>
    fun getAllFoodCounts(date: String): Flow<Resource<List<FoodCount>>>
    fun getAuditLogs(): Flow<List<AuditLog>>
    suspend fun clearOldLogs(beforeTimestamp: Long)
}
