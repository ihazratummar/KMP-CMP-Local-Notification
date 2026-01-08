package com.hazrat.learning.notificationapp.notification

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import androidx.core.net.toUri

actual class LocalNotificationManager(private val context: Context) {

    private val notificationManager = NotificationManagerCompat.from(context)
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        private const val CHANNEL_ID = "default_channel"
        private const val CHANNEL_NAME = "Default Notifications"
    }

    init {
        createNotificationChannel()
    }


    actual fun showNotification(title: String, body: String) {

        try {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            // Permission is checked above, safe to call notify
            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
            println("✅ Notification shown successfully")
        } catch (e: SecurityException) {
            // Handle edge case where permission was revoked between check and notify
            println("SecurityException when showing notification: ${e.message}")
        } catch (e: Exception) {
            println("Error showing notification: ${e.message}")
        }
    }

    actual fun scheduleNotification(id: String, title: String, body: String, timestamp: Long) {
        // Need SCHEDULE_EXACT_ALARM on Android 12+ (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                println("Cannot schedule exact alarm: Permission missing")
                // In a real app, we should guide user to settings: Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                return
            }
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("body", body)
            // Use ID hashcode as request code uniqueness
            data = "custom://$id".toUri()
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.hashCode(), // Unique Request Code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // Use setExactAndAllowWhileIdle for precision even in Doze mode
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timestamp,
                pendingIntent
            )
            println("⏰ Alarm scheduled for $timestamp (ID: $id)")
        } catch (e: SecurityException) {
             println("SecurityException scheduling alarm: ${e.message}")
        }
    }

    actual fun cancelNotification(id: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            data = "custom://$id".toUri()
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            println("❌ Alarm cancelled (ID: $id)")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Default notification channel"
            }

            val systemManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager
            systemManager.createNotificationChannel(channel)
        }
    }
}