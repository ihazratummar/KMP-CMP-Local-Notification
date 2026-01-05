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

        if (!repository.checkPermission()) {
            // Business Rule: If permission missing, try to request it once? 
            // Or fail? "Industry grade" usually means strict adherence to contract. 
            // If the UI is supposed to handle permission flow, we should fail here.
            // However, a robust app might try to recover.
            // Let's stick to failing to enforce proper UI flow (Check -> Request -> Send).
            return Result.failure(NotificationError.PermissionDenied)
        }

        return repository.sendNotification(title.trim(), body.trim())
    }
}