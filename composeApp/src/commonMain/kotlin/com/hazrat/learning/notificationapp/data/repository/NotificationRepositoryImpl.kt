package com.hazrat.learning.notificationapp.data.repository

import com.hazrat.learning.notificationapp.domain.model.NotificationError
import com.hazrat.learning.notificationapp.domain.repository.NotificationRepository
import com.hazrat.learning.notificationapp.notification.LocalNotificationManager

class NotificationRepositoryImpl(
    private val localNotificationManager: LocalNotificationManager
) : NotificationRepository {

    override suspend fun checkPermission(): Boolean {
        return localNotificationManager.hasPermission()
    }

    override suspend fun requestPermission(): Result<Boolean> {
        return try {
            val result = localNotificationManager.requestPermission()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(NotificationError.SystemError(e.message))
        }
    }

    override suspend fun sendNotification(title: String, body: String): Result<Unit> {
        return try {
            if (!localNotificationManager.hasPermission()) {
                return Result.failure(NotificationError.PermissionDenied)
            }
            localNotificationManager.showNotification(title, body)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(NotificationError.SystemError(e.message))
        }
    }

    override fun openAppSettings() {
        localNotificationManager.openAppSettings()
    }
}