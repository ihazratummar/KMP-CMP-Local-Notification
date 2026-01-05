package com.hazrat.learning.notificationapp.domain.model

sealed class NotificationError : Throwable() {
    data object PermissionDenied : NotificationError()
    data object InvalidContent : NotificationError()
    data class SystemError(override val message: String?) : NotificationError()
    data object Unknown : NotificationError()
}