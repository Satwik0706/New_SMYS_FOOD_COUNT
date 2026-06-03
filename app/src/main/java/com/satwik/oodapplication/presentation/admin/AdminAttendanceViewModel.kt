package com.satwik.oodapplication.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satwik.oodapplication.data.model.Attendance
import com.satwik.oodapplication.data.model.FoodCount
import com.satwik.oodapplication.data.model.User
import com.satwik.oodapplication.domain.repository.AttendanceRepository
import com.satwik.oodapplication.domain.repository.AuthRepository
import com.satwik.oodapplication.domain.repository.FoodCountRepository
import com.satwik.oodapplication.utils.Constants
import com.satwik.oodapplication.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AdminAttendanceViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val attendanceRepository: AttendanceRepository,
    private val foodCountRepository: FoodCountRepository
) : ViewModel() {

    private val _date = MutableStateFlow(LocalDate.now().toString())
    val date: StateFlow<String> = _date

    private val _students = MutableStateFlow<List<User>>(emptyList())
    val students: StateFlow<List<User>> = _students

    private val _attendanceMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val attendanceMap: StateFlow<Map<String, Boolean>> = _attendanceMap

    private val _missedFoodStudents = MutableStateFlow<List<User>>(emptyList())
    val missedFoodStudents: StateFlow<List<User>> = _missedFoodStudents

    private val _uiState = MutableStateFlow<Resource<Unit>?>(null)
    val uiState: StateFlow<Resource<Unit>?> = _uiState

    init {
        loadData()
        autoCleanup()
    }

    private fun autoCleanup() {
        viewModelScope.launch {
            // Delete records older than 2 days
            val cleanupDate = LocalDate.now().minusDays(2).toString()
            attendanceRepository.clearOldAttendance(cleanupDate)
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            authRepository.getUsersByRole(Constants.ROLE_STUDENT).collect {
                _students.value = it
            }
        }
        viewModelScope.launch {
            attendanceRepository.getAttendanceForDate(_date.value).collect { resource ->
                if (resource is Resource.Success) {
                    val map = resource.data?.associate { it.studentId to it.isPresent } ?: emptyMap()
                    _attendanceMap.value = map
                }
            }
        }
    }

    fun toggleAttendance(studentId: String) {
        val current = _attendanceMap.value.toMutableMap()
        val currentState = current[studentId] ?: true
        current[studentId] = !currentState
        _attendanceMap.value = current
    }

    fun acknowledgeReport() {
        // Revert flagged students to Present
        val currentMap = _attendanceMap.value.toMutableMap()
        _missedFoodStudents.value.forEach { student ->
            currentMap[student.uid] = true
        }
        _attendanceMap.value = currentMap
        _missedFoodStudents.value = emptyList()

        // Automatically submit after reverting
        viewModelScope.launch {
            _uiState.value = Resource.Loading()
            saveAttendanceInternal()
        }
    }

    fun submitAttendance() {
        viewModelScope.launch {
            _uiState.value = Resource.Loading()
            
            // 1. Pre-validation: Check for students marked Absent who have ordered Dinner
            // We use the first emission of the food counts flow
            val countsResource = foodCountRepository.getAllFoodCounts(_date.value).first()
            
            if (countsResource is Resource.Success) {
                val countsMap = (countsResource.data ?: emptyList()).associateBy { it.studentId }
                val missed = mutableListOf<User>()

                _students.value.forEach { student ->
                    val isPresent = _attendanceMap.value[student.uid] ?: true
                    if (!isPresent) {
                        val count = countsMap[student.uid]
                        // Night food logic: Check today's record OR fall back to user permanent preferences
                        val hasOrderedNightFood = if (count != null) {
                            !count.isLeave && count.dinner
                        } else {
                            student.dinnerPref
                        }

                        if (hasOrderedNightFood) {
                            missed.add(student)
                        }
                    }
                }

                if (missed.isNotEmpty()) {
                    _missedFoodStudents.value = missed
                    _uiState.value = null // Stop loading to show the dialog
                    return@launch
                }
            }

            // 2. If no conflicts found, proceed to save
            saveAttendanceInternal()
        }
    }

    private suspend fun saveAttendanceInternal() {
        val list = _students.value.map { student ->
            Attendance(
                studentId = student.uid,
                studentName = student.name,
                date = _date.value,
                isPresent = _attendanceMap.value[student.uid] ?: true,
                batch = student.year ?: "Unknown"
            )
        }
        val result = attendanceRepository.saveAttendance(list)
        _uiState.value = result
        
        if (result is Resource.Success) {
            // Log Admin action
            authRepository.getSession()?.let { admin ->
                authRepository.logAction(admin, "Submitted attendance for ${_date.value}")
            }
        }
    }
}
