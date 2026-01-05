package com.hazrat.learning.notificationapp.domain.usecase

import com.hazrat.learning.notificationapp.domain.repository.NotificationRepository

class ManagePermissionUseCase(
    private val repository: NotificationRepository
) {
    suspend fun checkPermission(): Boolean = repository.checkPermission()

    suspend fun requestPermission(): Result<Boolean> = repository.requestPermission()

    fun openSettings() = repository.openAppSettings()
}