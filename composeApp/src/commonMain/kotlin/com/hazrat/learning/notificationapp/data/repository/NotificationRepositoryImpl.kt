package com.hazrat.learning.notificationapp.data.repository

import com.hazrat.learning.notificationapp.domain.model.NotificationError
import com.hazrat.learning.notificationapp.domain.repository.NotificationRepository
import com.hazrat.learning.notificationapp.notification.LocalNotificationManager

import com.hazrat.learning.notificationapp.domain.model.ScheduledNotification

class NotificationRepositoryImpl(
    private val localNotificationManager: LocalNotificationManager
) : NotificationRepository {

    // In-memory storage for demonstration (Replace with Room/DataStore for production)
    private val scheduledItems = mutableListOf<ScheduledNotification>()

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

    override suspend fun scheduleNotification(item: ScheduledNotification): Result<Unit> {
         return try {
            if (!localNotificationManager.hasPermission()) {
                return Result.failure(NotificationError.PermissionDenied)
            }
            // Add to local list
            scheduledItems.add(item)
            // Schedule in system
            localNotificationManager.scheduleNotification(item.id, item.title, item.body, item.scheduledTime)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(NotificationError.SystemError(e.message))
        }
    }

    override suspend fun cancelNotification(id: String): Result<Unit> {
        return try {
            localNotificationManager.cancelNotification(id)
            scheduledItems.removeAll { it.id == id }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(NotificationError.SystemError(e.message))
        }
    }

    override suspend fun getScheduledNotifications(): Result<List<ScheduledNotification>> {
        return Result.success(scheduledItems.toList())
    }

    override fun openAppSettings() {
        localNotificationManager.openAppSettings()
    }
}