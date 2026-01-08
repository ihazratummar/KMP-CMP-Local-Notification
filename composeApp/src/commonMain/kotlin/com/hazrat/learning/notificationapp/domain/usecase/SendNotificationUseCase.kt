package com.hazrat.learning.notificationapp.domain.usecase

import com.hazrat.learning.notificationapp.domain.model.NotificationError
import com.hazrat.learning.notificationapp.domain.repository.NotificationRepository

class SendNotificationUseCase(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(title: String, body: String): Result<Unit> {
        if (title.isBlank() || body.isBlank()) {
            return Result.failure(NotificationError.InvalidContent)
        }

        return repository.sendNotification(title.trim(), body.trim())
    }
}