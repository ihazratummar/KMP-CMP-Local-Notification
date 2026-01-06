package com.hazrat.learning.notificationapp.domain.usecase

import com.hazrat.learning.notificationapp.domain.model.ScheduledNotification
import com.hazrat.learning.notificationapp.domain.repository.NotificationRepository

class GetScheduledNotificationsUseCase(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(): Result<List<ScheduledNotification>> {
        return repository.getScheduledNotifications()
    }
}