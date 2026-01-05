package com.hazrat.learning.notificationapp.domain.repository

interface NotificationRepository {
    suspend fun checkPermission(): Boolean
    suspend fun requestPermission(): Result<Boolean>
    suspend fun sendNotification(title: String, body: String): Result<Unit>
    fun openAppSettings()
}