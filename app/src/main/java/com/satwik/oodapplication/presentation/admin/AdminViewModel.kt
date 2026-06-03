package com.satwik.oodapplication.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satwik.oodapplication.data.model.LockStatus
import com.satwik.oodapplication.domain.repository.AuthRepository
import com.satwik.oodapplication.domain.repository.FoodCountRepository
import com.satwik.oodapplication.utils.Constants
import com.satwik.oodapplication.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AdminSummary(
    val totalStudents: Int = 0,
    val presentStudents: Int = 0,
    val countSubmitted: Int = 0
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val foodCountRepository: FoodCountRepository
) : ViewModel() {

    private val _summary = MutableStateFlow<AdminSummary>(AdminSummary())
    val summary: StateFlow<AdminSummary> = _summary

    private val _lockStatus = MutableStateFlow<LockStatus>(LockStatus())
    val lockStatus: StateFlow<LockStatus> = _lockStatus

    init {
        loadSummary()
        loadLockStatus()
    }

    private fun loadLockStatus() {
        val today = LocalDate.now().toString()
        viewModelScope.launch {
            foodCountRepository.getLockStatus(today).collect { resource ->
                if (resource is Resource.Success) {
                    _lockStatus.value = resource.data ?: LockStatus(date = today)
                }
            }
        }
    }

    fun toggleLock() {
        val today = LocalDate.now().toString()
        val current = _lockStatus.value
        val newStatus = current.copy(date = today, locked = !current.locked)
        viewModelScope.launch {
            foodCountRepository.updateLockStatus(newStatus)
            _lockStatus.value = newStatus
            
            // Log Admin action
            authRepository.getSession()?.let { admin ->
                val action = if (newStatus.locked) "Locked Portal" else "Unlocked Portal"
                authRepository.logAction(admin, action)
            }
        }
    }

    fun toggleMealLock(mealType: String) {
        val today = LocalDate.now().toString()
        val current = _lockStatus.value
        val newStatus = when (mealType.lowercase()) {
            "breakfast" -> current.copy(breakfastLocked = !current.breakfastLocked)
            "lunch" -> current.copy(lunchLocked = !current.lunchLocked)
            "snack" -> current.copy(snackLocked = !current.snackLocked)
            "dinner" -> current.copy(dinnerLocked = !current.dinnerLocked)
            else -> current
        }.copy(date = today)

        viewModelScope.launch {
            foodCountRepository.updateLockStatus(newStatus)
            _lockStatus.value = newStatus
            
            // Log Admin action
            authRepository.getSession()?.let { admin ->
                val isLocked = when (mealType.lowercase()) {
                    "breakfast" -> newStatus.breakfastLocked
                    "lunch" -> newStatus.lunchLocked
                    "snack" -> newStatus.snackLocked
                    "dinner" -> newStatus.dinnerLocked
                    else -> false
                }
                val action = if (isLocked) "Locked $mealType" else "Unlocked $mealType"
                authRepository.logAction(admin, action)
            }
        }
    }

    fun loadSummary() {
        val today = LocalDate.now().toString()
        viewModelScope.launch {
            combine(
                authRepository.getUsersByRole(Constants.ROLE_STUDENT),
                foodCountRepository.getAllFoodCounts(today)
            ) { students, countsResource ->
                val countsMap = (countsResource as? Resource.Success)?.data?.associateBy { it.studentId } ?: emptyMap()
                
                var totalEatingAnyMeal = 0
                students.forEach { student ->
                    val count = countsMap[student.uid]
                    val isEatingToday = if (count != null) {
                        !count.isLeave && (count.breakfast || count.lunch || count.snack || count.dinner || count.lunchBox)
                    } else {
                        student.breakfastPref || student.lunchPref || student.snackPref || student.dinnerPref
                    }
                    if (isEatingToday) totalEatingAnyMeal++
                }

                AdminSummary(
                    totalStudents = students.size,
                    presentStudents = students.size,
                    countSubmitted = totalEatingAnyMeal
                )
            }.collect {
                _summary.value = it
            }
        }
    }
}
