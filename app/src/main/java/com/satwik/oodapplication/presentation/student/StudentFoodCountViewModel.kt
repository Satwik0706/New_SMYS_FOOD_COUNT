package com.satwik.oodapplication.presentation.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satwik.oodapplication.data.model.FoodCount
import com.satwik.oodapplication.data.model.LockStatus
import com.satwik.oodapplication.data.model.User
import com.satwik.oodapplication.domain.repository.AuthRepository
import com.satwik.oodapplication.domain.repository.FoodCountRepository
import com.satwik.oodapplication.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class StudentFoodCountViewModel @Inject constructor(
    private val repository: FoodCountRepository
) : ViewModel() {

    private val _foodCountState = MutableStateFlow<Resource<FoodCount>>(Resource.Loading())
    val foodCountState: StateFlow<Resource<FoodCount>> = _foodCountState

    private val _lockStatus = MutableStateFlow<Resource<LockStatus>>(Resource.Loading())
    val lockStatus: StateFlow<Resource<LockStatus>> = _lockStatus

    fun loadData(studentId: String, date: String = LocalDate.now().toString()) {
        viewModelScope.launch {
            repository.getStudentFoodCount(studentId, date).collect {
                _foodCountState.value = it
            }
        }
        viewModelScope.launch {
            repository.getLockStatus(date).collect {
                _lockStatus.value = it
            }
        }
    }

    fun toggleMeal(studentId: String, date: String, mealType: String) {
        val lock = (_lockStatus.value as? Resource.Success)?.data
        val masterLocked = lock?.locked ?: false
        if (masterLocked) return

        val currentResource = _foodCountState.value
        if (currentResource !is Resource.Success) return
        
        val current = currentResource.data!!
        
        // Master Lock Logic: If isLeave is ON, nothing else can be toggled except turning isLeave OFF
        if (current.isLeave && mealType.lowercase() != "leave") return

        val updated = when (mealType.lowercase()) {
            "breakfast" -> {
                if (lock?.breakfastLocked == true) return
                val newBreakfast = !current.breakfast
                val newLunchBox = if (!newBreakfast) false else current.lunchBox
                current.copy(breakfast = newBreakfast, lunchBox = newLunchBox)
            }
            "lunchbox" -> {
                if (lock?.breakfastLocked == true) return
                val newLunchBox = !current.lunchBox
                if (newLunchBox) {
                    current.copy(lunchBox = true, breakfast = true, lunch = false)
                } else {
                    current.copy(lunchBox = false)
                }
            }
            "lunch" -> {
                if (lock?.lunchLocked == true) return
                val newLunch = !current.lunch
                if (newLunch) {
                    current.copy(lunch = true, lunchBox = false)
                } else {
                    current.copy(lunch = false)
                }
            }
            "snack" -> {
                if (lock?.snackLocked == true) return
                current.copy(snack = !current.snack)
            }
            "dinner" -> {
                if (lock?.dinnerLocked == true) return
                current.copy(dinner = !current.dinner)
            }
            "leave" -> {
                // Cannot toggle leave if any of the main meals are locked
                if (lock?.breakfastLocked == true || lock?.lunchLocked == true || lock?.dinnerLocked == true) return

                val newState = !current.isLeave
                if (newState) {
                    // Toggling Leave ON: Master lock and set everything to 0
                    current.copy(isLeave = true, breakfast = false, lunch = false, snack = false, dinner = false, lunchBox = false)
                } else {
                    // Toggling Leave OFF: Unlock everything
                    current.copy(isLeave = false)
                }
            }
            else -> current
        }

        viewModelScope.launch {
            // Apply immediately to local state for fast UI
            _foodCountState.value = Resource.Success(updated)
            // Persist to Firebase
            repository.submitFoodCount(updated)
        }
    }
}
