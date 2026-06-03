package com.satwik.oodapplication.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satwik.oodapplication.data.model.MealInfo
import com.satwik.oodapplication.data.model.Menu
import com.satwik.oodapplication.domain.repository.AuthRepository
import com.satwik.oodapplication.domain.repository.MenuRepository
import com.satwik.oodapplication.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MenuManagementViewModel @Inject constructor(
    private val repository: MenuRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _menuState = MutableStateFlow<Resource<Menu>>(Resource.Loading())
    val menuState: StateFlow<Resource<Menu>> = _menuState

    private val _updateState = MutableStateFlow<Resource<Unit>?>(null)
    val updateState: StateFlow<Resource<Unit>?> = _updateState

    init {
        autoCleanup()
    }

    private fun autoCleanup() {
        viewModelScope.launch {
            // Delete records older than 6 days
            val cleanupDate = LocalDate.now().minusDays(6).toString()
            repository.clearOldMenu(cleanupDate)
        }
    }

    fun loadMenu(date: String = LocalDate.now().toString()) {
        viewModelScope.launch {
            repository.getMenu(date).collect {
                _menuState.value = it
            }
        }
    }

    fun updateMeal(date: String, mealType: String, items: List<String>, timing: String) {
        val currentMenu = (_menuState.value as? Resource.Success)?.data ?: Menu(date = date)
        val mealInfo = MealInfo(items = items, timing = timing)
        
        val updatedMenu = when (mealType.lowercase()) {
            "breakfast" -> currentMenu.copy(breakfast = mealInfo)
            "lunch" -> currentMenu.copy(lunch = mealInfo)
            "snack" -> currentMenu.copy(snack = mealInfo)
            "dinner" -> currentMenu.copy(dinner = mealInfo)
            else -> currentMenu
        }

        viewModelScope.launch {
            _updateState.value = Resource.Loading()
            _updateState.value = repository.updateMenu(updatedMenu)
            
            // Log Admin action
            authRepository.getSession()?.let { admin ->
                authRepository.logAction(admin, "Updated $mealType menu for $date")
            }

            loadMenu(date)
        }
    }
}
