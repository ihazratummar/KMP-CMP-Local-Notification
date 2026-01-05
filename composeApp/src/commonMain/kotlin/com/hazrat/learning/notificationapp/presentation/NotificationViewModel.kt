package com.hazrat.learning.notificationapp.presentation


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hazrat.learning.notificationapp.domain.model.NotificationError
import com.hazrat.learning.notificationapp.domain.usecase.ManagePermissionUseCase
import com.hazrat.learning.notificationapp.domain.usecase.SendNotificationUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class NotificationUiState(
    val title: String = "Meeting Reminder",
    val body: String = "Don't forget the team sync at 10 AM.",
    val isPermissionGranted: Boolean = false,
    val showSuccessMessage: Boolean = false,
    val isRequestingPermission: Boolean = false,
    val errorMessage: String? = null
)

class NotificationViewModel(
    private val sendNotificationUseCase: SendNotificationUseCase,
    private val managePermissionUseCase: ManagePermissionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        startPermissionPolling()
    }

    private fun startPermissionPolling() {
        viewModelScope.launch {
            while (isActive) {
                checkPermissionStatus()
                delay(1000) // Poll every second
            }
        }
    }

    private suspend fun checkPermissionStatus() {
        val granted = managePermissionUseCase.checkPermission()
        _uiState.update { it.copy(isPermissionGranted = granted) }
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun onBodyChange(newBody: String) {
        _uiState.update { it.copy(body = newBody) }
    }

    fun togglePermission(shouldEnable: Boolean) {
        if (shouldEnable) {
            requestPermission()
        } else {
            managePermissionUseCase.openSettings()
        }
    }

    private fun requestPermission() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRequestingPermission = true) }
            
            managePermissionUseCase.requestPermission()
                .onSuccess { granted ->
                     _uiState.update { 
                        it.copy(
                            isPermissionGranted = granted,
                            isRequestingPermission = false,
                            errorMessage = if (!granted) "Permission denied. Please enable from settings." else null
                        ) 
                    }
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isRequestingPermission = false,
                            errorMessage = "Failed to request permission: ${error.message}"
                        ) 
                    }
                }
        }
    }
    
    fun openSettings() {
        managePermissionUseCase.openSettings()
    }

    fun sendNotification() {
        val currentState = _uiState.value
        
        viewModelScope.launch {
            sendNotificationUseCase(currentState.title, currentState.body)
                .onSuccess {
                    showSuccess()
                    _uiState.update { it.copy(errorMessage = null) }
                }
                .onFailure { error ->
                    val msg = when(error) {
                        is NotificationError.PermissionDenied -> "Permission required to send notifications"
                        is NotificationError.InvalidContent -> "Title and body cannot be empty"
                        is NotificationError.SystemError -> "System error: ${error.message}"
                        else -> "An unknown error occurred"
                    }
                    _uiState.update { it.copy(errorMessage = msg) }
                }
        }
    }

    private fun showSuccess() {
        viewModelScope.launch {
            _uiState.update { it.copy(showSuccessMessage = true) }
            delay(3000)
            _uiState.update { it.copy(showSuccessMessage = false) }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}