package com.satwik.oodapplication.domain.repository

import com.satwik.oodapplication.data.model.User
import com.satwik.oodapplication.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun login(identifier: String, pass: String): Flow<Resource<User>>
    fun forgotPassword(identifier: String): Flow<Resource<String>>
    fun logout()
    fun getCurrentUser(uid: String): Flow<Resource<User>>
    suspend fun resetPassword(uid: String, newPass: String)
    fun getSession(): User?
    fun getAllUsers(): Flow<List<User>>
    fun getUsersByRole(role: String): Flow<List<User>>
    suspend fun addUser(user: User, pass: String)
    suspend fun deleteUser(uid: String)
    fun logAction(user: User, action: String)
}
