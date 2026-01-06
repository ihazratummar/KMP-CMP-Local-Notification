package com.hazrat.learning.notificationapp.domain.usecase

import com.hazrat.learning.notificationapp.domain.repository.NotificationRepository

class CancelNotificationUseCase(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return repository.cancelNotification(id)
    }
}