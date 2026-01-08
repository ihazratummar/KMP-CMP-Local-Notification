package com.hazrat.learning.notificationapp.presentation


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hazrat.learning.notificationapp.domain.model.NotificationError
import com.hazrat.learning.notificationapp.domain.model.ScheduledNotification
import com.hazrat.learning.notificationapp.domain.usecase.CancelNotificationUseCase
import com.hazrat.learning.notificationapp.domain.usecase.GetScheduledNotificationsUseCase
import com.hazrat.learning.notificationapp.domain.usecase.ScheduleNotificationUseCase
import com.hazrat.learning.notificationapp.domain.usecase.SendNotificationUseCase
import com.hazrat.learning.notificationapp.notification.LocalNotificationManager
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.notifications.REMOTE_NOTIFICATION
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Clock

data class NotificationUiState(
    val title: String = "Meeting Reminder",
    val body: String = "Don't forget the team sync at 10 AM.",
    val isPermissionGranted: Boolean = false,
    val showSuccessMessage: Boolean = false,
    val isRequestingPermission: Boolean = false,
    val errorMessage: String? = null,
    val scheduledNotifications: List<ScheduledNotification> = emptyList(),
    // Schedule Input State
    val scheduleDelaySeconds: Int = 10 
)

class NotificationViewModel(
    private val sendNotificationUseCase: SendNotificationUseCase,
    private val scheduleNotificationUseCase: ScheduleNotificationUseCase,
    private val cancelNotificationUseCase: CancelNotificationUseCase,
    private val getScheduledNotificationsUseCase: GetScheduledNotificationsUseCase,
    private val permissionManager: NotificationPermissionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        loadScheduledNotifications()
        refreshPermissionState()
    }

    fun refreshPermissionState() {
        viewModelScope.launch {
            val granted = permissionManager.isGranted(permission = Permission.REMOTE_NOTIFICATION)
            _uiState.update { it.copy(isPermissionGranted = granted) }
        }
    }

    private fun loadScheduledNotifications() {
        viewModelScope.launch {
             getScheduledNotificationsUseCase()
                 .onSuccess { list ->
                     _uiState.update { it.copy(scheduledNotifications = list) }
                 }
        }
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun onBodyChange(newBody: String) {
        _uiState.update { it.copy(body = newBody) }
    }

    fun onDelayChange(seconds: Int) {
         _uiState.update { it.copy(scheduleDelaySeconds = seconds) }
    }

    fun togglePermission(shouldEnable: Boolean) {
        if (shouldEnable) {
            requestPermission()
        } else {
            openSettings()
        }
    }

    fun requestPermission() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRequestingPermission = true) }

            val granted = permissionManager.request(permission = Permission.REMOTE_NOTIFICATION)

            _uiState.update {
                it.copy(
                    isPermissionGranted = granted,
                    isRequestingPermission = false,
                    errorMessage = if (granted) null else "Permission denied"
                )
            }
        }
    }

    fun openSettings() {
        permissionManager.openSettings()
    }

    fun sendNotification() {
        val currentState = _uiState.value

        viewModelScope.launch {
            sendNotificationUseCase(currentState.title, currentState.body)
                .onSuccess {
                    showSuccess()
                    _uiState.update { it.copy(errorMessage = null) }
                }
                .onFailure { error -> handleError(error) }
        }
    }

    fun scheduleNotification() {
        val currentState = _uiState.value
        val scheduledTime = Clock.System.now().toEpochMilliseconds() + (currentState.scheduleDelaySeconds * 1000)

        viewModelScope.launch {
            scheduleNotificationUseCase(
                title = currentState.title,
                body = currentState.body,
                timestamp = scheduledTime
            ).onSuccess {
                showSuccess()
                _uiState.update { it.copy(errorMessage = null) }
                loadScheduledNotifications()
            }.onFailure { error -> handleError(error) }
        }
    }

    fun cancelNotification(id: String) {
        viewModelScope.launch {
            cancelNotificationUseCase(id)
                .onSuccess {
                    loadScheduledNotifications()
                }
                .onFailure { error -> handleError(error) }
        }
    }

    private fun handleError(error: Throwable) {
        val msg = when(error) {
            is NotificationError.PermissionDenied -> "Permission required"
            is NotificationError.InvalidContent -> "Invalid content or time"
            is NotificationError.SystemError -> "System error: ${error.message}"
            else -> "Error: ${error.message}"
        }
        _uiState.update { it.copy(errorMessage = msg) }
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