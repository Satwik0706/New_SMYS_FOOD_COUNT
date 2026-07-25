package com.satwik.oodapplication.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satwik.oodapplication.data.model.SnackStatus
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

data class BatchSnackCount(
    val batchName: String,
    val count: Int
)

@HiltViewModel
class AdminSnackViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val foodCountRepository: FoodCountRepository
) : ViewModel() {

    private val _today = LocalDate.now().toString()

    private val _snackStatus = MutableStateFlow<Resource<SnackStatus>>(Resource.Loading())
    val snackStatus: StateFlow<Resource<SnackStatus>> = _snackStatus

    val snackReport: StateFlow<Resource<List<BatchSnackCount>>> = combine(
        authRepository.getUsersByRole(Constants.ROLE_STUDENT),
        foodCountRepository.getAllFoodCounts(_today)
    ) { students, countsResource ->
        if (countsResource is Resource.Loading) return@combine Resource.Loading()
        if (countsResource is Resource.Error) return@combine Resource.Error(countsResource.message ?: "Error")

        val countsMap = (countsResource as? Resource.Success)?.data?.associateBy { it.studentId } ?: emptyMap()
        val batchMap = mutableMapOf<String, Int>()

        students.forEach { student ->
            val daily = countsMap[student.uid]
            val onLeave = daily?.isOnLeave ?: student.isLeave
            
            if (!onLeave) {
                val isEatingSnack = daily?.isSnack ?: student.snackPref
                if (isEatingSnack) {
                    val batch = student.year ?: "Unknown"
                    batchMap[batch] = batchMap.getOrDefault(batch, 0) + 1
                }
            }
        }

        Resource.Success(
            batchMap.map { BatchSnackCount(it.key, it.value) }.sortedBy { it.batchName }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading())

    private val _uiState = MutableStateFlow<Resource<Unit>?>(null)
    val uiState: StateFlow<Resource<Unit>?> = _uiState

    init {
        loadSnackStatus()
    }

    private fun loadSnackStatus() {
        viewModelScope.launch {
            foodCountRepository.getSnackStatus().collect {
                _snackStatus.value = it
            }
        }
    }

    fun toggleSnackLock() {
        val current = (_snackStatus.value as? Resource.Success)?.data?.locked ?: false
        viewModelScope.launch {
            _uiState.value = Resource.Loading()
            val result = foodCountRepository.updateSnackLock(!current)
            _uiState.value = result
            
            // Clear result after 2 seconds
            kotlinx.coroutines.delay(2000)
            _uiState.value = null
        }
    }
}
