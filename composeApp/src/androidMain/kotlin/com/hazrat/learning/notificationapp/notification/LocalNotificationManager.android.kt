package com.hazrat.learning.notificationapp.notification

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class LocalNotificationManager(private val context: Context) {

    private val notificationManager = NotificationManagerCompat.from(context)

    // Store the permission callback
    private var permissionCallback: ((Boolean) -> Unit)? = null

    // Activity result launcher for permissions (set from Activity)
    private var permissionLauncher: ActivityResultLauncher<String>? = null

    companion object {
        private const val CHANNEL_ID = "default_channel"
        private const val CHANNEL_NAME = "Default Notifications"
        const val PERMISSION_REQUEST_CODE = 1001
    }

    init {
        createNotificationChannel()
        setupPermissionLauncher()
    }

    private fun setupPermissionLauncher() {
        // Try to set up the launcher if context is ComponentActivity
        (context as? ComponentActivity)?.let { activity ->
            permissionLauncher = activity.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                permissionCallback?.invoke(granted)
                permissionCallback = null
            }
        }
    }

    // Actual implementation for Android
    actual fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ requires runtime permission
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Below Android 13, check if notifications are enabled
            notificationManager.areNotificationsEnabled()
        }
    }

    actual suspend fun requestPermission(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                // Below Android 13 - check if enabled, if not open settings
                val enabled = notificationManager.areNotificationsEnabled()
                if (!enabled) {
                    openAppSettings()
                }
                continuation.resume(enabled)
                return@suspendCancellableCoroutine
            }

            // Check if already granted
            if (hasPermission()) {
                continuation.resume(true)
                return@suspendCancellableCoroutine
            }

            // Use ActivityResultLauncher if available
            if (permissionLauncher != null) {
                permissionCallback = { granted ->
                    continuation.resume(granted)
                }
                permissionLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Fallback: Try to use old method
                val activity = context as? Activity
                if (activity != null) {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        PERMISSION_REQUEST_CODE
                    )
                    // Note: This won't wait for result, need to handle in Activity
                    continuation.resume(false)
                } else {
                    println("ERROR: Context is not an Activity. Cannot request permission.")
                    continuation.resume(false)
                }
            }
        }
    }

    // Call this from Activity's onRequestPermissionsResult
    fun handlePermissionResult(requestCode: Int, granted: Boolean) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            permissionCallback?.invoke(granted)
            permissionCallback = null
        }
    }

    actual fun openAppSettings() {
        try {
            val intent = Intent().apply {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    else -> {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            println("Failed to open notification settings: ${e.message}")
        }
    }

    actual fun showNotification(title: String, body: String) {
        // LINT FIX: Check permission before showing notification
        if (!hasPermission()) {
            println("Cannot show notification: Permission not granted")
            return
        }

        try {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Needed
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