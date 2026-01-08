package com.hazrat.learning.notificationapp.domain.usecase

import com.hazrat.learning.notificationapp.domain.model.NotificationError
import com.hazrat.learning.notificationapp.domain.model.ScheduledNotification
import com.hazrat.learning.notificationapp.domain.repository.NotificationRepository
import kotlin.time.Clock.System

class ScheduleNotificationUseCase(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(title: String, body: String, timestamp: Long): Result<Unit> {
        if (title.isBlank() || body.isBlank()) {
            return Result.failure(NotificationError.InvalidContent)
        }

        val currentTime = System.now().toEpochMilliseconds()
        if (timestamp <= currentTime) {
            return Result.failure(NotificationError.InvalidContent) // Should use a better error like "PastTime"
        }

        val id = timestamp.toString() // Simple ID generation strategy
        val notification = ScheduledNotification(
            id = id,
            title = title,
            body = body,
            scheduledTime = timestamp
        )

        return repository.scheduleNotification(notification)
    }
}