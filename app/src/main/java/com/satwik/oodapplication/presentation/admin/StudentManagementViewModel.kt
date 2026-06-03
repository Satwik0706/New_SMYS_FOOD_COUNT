package com.satwik.oodapplication.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satwik.oodapplication.data.model.User
import com.satwik.oodapplication.domain.repository.AuthRepository
import com.satwik.oodapplication.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class StudentManagementViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _students = MutableStateFlow<List<User>>(emptyList())
    val students: StateFlow<List<User>> = _students

    init {
        loadStudents()
    }

    private fun loadStudents() {
        viewModelScope.launch {
            repository.getUsersByRole(Constants.ROLE_STUDENT).collect {
                _students.value = it
            }
        }
    }

    fun addStudent(name: String, email: String, year: String, rollNumber: String) {
        viewModelScope.launch {
            val newUser = User(
                uid = UUID.randomUUID().toString(),
                name = name,
                email = email,
                role = Constants.ROLE_STUDENT,
                year = year,
                rollNumber = rollNumber
            )
            repository.addUser(newUser, rollNumber)
            
            // Log Admin action
            repository.getSession()?.let { admin ->
                repository.logAction(admin, "Enrolled student: $name")
            }
        }
    }

    fun deleteStudent(uid: String) {
        viewModelScope.launch {
            val studentName = _students.value.find { it.uid == uid }?.name ?: "Unknown"
            repository.deleteUser(uid)
            
            // Log Admin action
            repository.getSession()?.let { admin ->
                repository.logAction(admin, "Deleted student: $studentName")
            }
        }
    }

    fun updateStudent(user: User, newPassword: String?) {
        viewModelScope.launch {
            repository.addUser(user, newPassword ?: user.rollNumber ?: "")
            
            // Log Admin action
            repository.getSession()?.let { admin ->
                repository.logAction(admin, "Updated details for: ${user.name}")
            }
        }
    }
}
