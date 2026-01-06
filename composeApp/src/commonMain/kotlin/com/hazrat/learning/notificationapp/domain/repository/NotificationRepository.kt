package com.hazrat.learning.notificationapp.domain.repository

import com.hazrat.learning.notificationapp.domain.model.ScheduledNotification

interface NotificationRepository {
    suspend fun checkPermission(): Boolean
    suspend fun requestPermission(): Result<Boolean>
    suspend fun sendNotification(title: String, body: String): Result<Unit>
    suspend fun scheduleNotification(item: ScheduledNotification): Result<Unit>
    suspend fun cancelNotification(id: String): Result<Unit>
    suspend fun getScheduledNotifications(): Result<List<ScheduledNotification>>
    fun openAppSettings()
}