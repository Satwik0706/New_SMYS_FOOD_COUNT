package com.satwik.oodapplication.presentation.manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satwik.oodapplication.data.model.AuditLog
import com.satwik.oodapplication.data.model.User
import com.satwik.oodapplication.domain.repository.AuthRepository
import com.satwik.oodapplication.domain.repository.FoodCountRepository
import com.satwik.oodapplication.utils.Constants
import com.satwik.oodapplication.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ManagerStats(
    val adminCount: Int = 0,
    val studentCount: Int = 0,
    val submittedToday: Int = 0
)

@HiltViewModel
class ManagerViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val foodCountRepository: FoodCountRepository
) : ViewModel() {

    private val _stats = MutableStateFlow(ManagerStats())
    val stats: StateFlow<ManagerStats> = _stats

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers

    private val _logs = MutableStateFlow<List<AuditLog>>(emptyList())
    val logs: StateFlow<List<AuditLog>> = _logs

    init {
        loadStats()
        loadAllUsers()
        loadLogs()
        autoCleanupLogs()
    }

    private fun loadStats() {
        val today = LocalDate.now().toString()
        viewModelScope.launch {
            combine(
                authRepository.getUsersByRole(Constants.ROLE_ADMIN),
                authRepository.getUsersByRole(Constants.ROLE_STUDENT),
                foodCountRepository.getAllFoodCounts(today)
            ) { admins, students, counts ->
                ManagerStats(
                    adminCount = admins.size,
                    studentCount = students.size,
                    submittedToday = (counts as? Resource.Success)?.data?.size ?: 0
                )
            }.collect {
                _stats.value = it
            }
        }
    }

    private fun loadAllUsers() {
        viewModelScope.launch {
            authRepository.getAllUsers().collect {
                _allUsers.value = it
            }
        }
    }

    private fun loadLogs() {
        viewModelScope.launch {
            foodCountRepository.getAuditLogs().collect {
                _logs.value = it
            }
        }
    }

    private fun autoCleanupLogs() {
        viewModelScope.launch {
            val twoDaysAgo = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000)
            foodCountRepository.clearOldLogs(twoDaysAgo)
        }
    }

    fun resetUserPassword(uid: String, newPass: String) {
        viewModelScope.launch {
            authRepository.resetPassword(uid, newPass)
        }
    }

    fun updateStudentPreferences(user: User) {
        viewModelScope.launch {
            authRepository.addUser(user, user.password ?: "")
        }
    }
}
