package com.satwik.oodapplication.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satwik.oodapplication.data.model.User
import com.satwik.oodapplication.domain.repository.AuthRepository
import com.satwik.oodapplication.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<Resource<User>?>(null)
    val authState: StateFlow<Resource<User>?> = _authState

    private val _userSession = MutableStateFlow<User?>(null)
    val userSession: StateFlow<User?> = _userSession

    private val _forgotPasswordState = MutableStateFlow<Resource<String>?>(null)
    val forgotPasswordState: StateFlow<Resource<String>?> = _forgotPasswordState

    private val _isUpdateRequired = MutableStateFlow(false)
    val isUpdateRequired: StateFlow<Boolean> = _isUpdateRequired

    private val _updateUrl = MutableStateFlow("")
    val updateUrl: StateFlow<String> = _updateUrl

    init {
        checkSession()
        checkAppVersion()
    }

    private fun checkAppVersion() {
        viewModelScope.launch {
            repository.getAppConfig().collect { resource ->
                if (resource is Resource.Success) {
                    val config = resource.data
                    if (config != null) {
                        _isUpdateRequired.value = com.satwik.oodapplication.BuildConfig.VERSION_CODE < config.minVersionCode
                        _updateUrl.value = config.updateUrl
                    }
                }
            }
        }
    }

    private fun checkSession() {
        val session = repository.getSession()
        if (session != null) {
            // Set session immediately from local storage for fast navigation
            _userSession.value = session
            
            // Sync with server in background to get full details
            viewModelScope.launch {
                repository.getCurrentUser(session.uid).collect { resource ->
                    if (resource is Resource.Success) {
                        val user = resource.data
                        _userSession.value = user
                    }
                }
            }
        }
    }

    fun login(identifier: String, pass: String) {
        _authState.value = null // Clear previous state
        viewModelScope.launch {
            repository.login(identifier, pass).collect {
                _authState.value = it
                if (it is Resource.Success) {
                    val user = it.data
                    _userSession.value = user
                }
            }
        }
    }

    fun forgotPassword(identifier: String) {
        viewModelScope.launch {
            repository.forgotPassword(identifier).collect {
                _forgotPasswordState.value = it
            }
        }
    }

    fun logout() {
        repository.logout()
        _userSession.value = null
        _authState.value = null
    }
}
