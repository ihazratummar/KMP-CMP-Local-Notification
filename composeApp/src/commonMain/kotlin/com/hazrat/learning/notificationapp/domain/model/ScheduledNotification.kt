package com.hazrat.learning.notificationapp.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

data class ScheduledNotification(
    val id: String,
    val title: String,
    val body: String,
    val scheduledTime: Long, // Epoch milliseconds
    val createdAt: Long = Clock.System.now().toEpochMilliseconds()
) {
    fun formatScheduledTime(): String {
        val instant = Instant.fromEpochMilliseconds(scheduledTime)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        // Simple manual formatting or use a formatter library if strictly needed
        // For now: YYYY-MM-DD HH:mm
        return "${dateTime.date} ${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
    }
}