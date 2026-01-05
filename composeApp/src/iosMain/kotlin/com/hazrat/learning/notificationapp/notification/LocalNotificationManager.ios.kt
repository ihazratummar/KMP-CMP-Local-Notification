package com.hazrat.learning.notificationapp.notification

// The 'actual' keyword says: "Here's the iOS implementation"
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSDate
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenNotificationSettingsURLString
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.concurrent.Volatile
import kotlin.coroutines.resume

// The 'actual' keyword says: "Here's the iOS implementation"
@OptIn(ExperimentalForeignApi::class)
actual class LocalNotificationManager {

    private val center = UNUserNotificationCenter.currentNotificationCenter()

    // Cache the permission state
    @Volatile
    private var cachedPermissionState: Boolean? = null

    init {
        // Update cache on initialization
        updatePermissionCacheSync()
    }

    // Actual implementation for iOS
    actual fun hasPermission(): Boolean {
        // Return cached state if available
        return cachedPermissionState ?: false
    }

    // Synchronous cache update (called from init)
    private fun updatePermissionCacheSync() {
        center.getNotificationSettingsWithCompletionHandler { settings ->
            val granted = settings?.authorizationStatus == UNAuthorizationStatusAuthorized ||
                    settings?.authorizationStatus == UNAuthorizationStatusProvisional
            cachedPermissionState = granted
            println("iOS Permission cached: $granted")
        }
    }

    // Internal method to update cache (called by requestPermission)
    private fun updatePermissionCache(completion: (Boolean) -> Unit) {
        center.getNotificationSettingsWithCompletionHandler { settings ->
            val granted = settings?.authorizationStatus == UNAuthorizationStatusAuthorized ||
                    settings?.authorizationStatus == UNAuthorizationStatusProvisional
            cachedPermissionState = granted
            println("iOS Permission updated: $granted")
            completion(granted)
        }
    }

    actual fun openAppSettings() {
        // Try notification settings first (iOS 15.4+)
        val notificationSettingsUrl = NSURL.URLWithString(UIApplicationOpenNotificationSettingsURLString)

        val urlToOpen = if (notificationSettingsUrl != null &&
            UIApplication.sharedApplication.canOpenURL(notificationSettingsUrl)) {
            notificationSettingsUrl
        } else {
            // Fallback to general app settings for older iOS versions
            NSURL.URLWithString(UIApplicationOpenSettingsURLString)
        }

        urlToOpen?.let { url ->
            UIApplication.sharedApplication.openURL(
                url = url,
                options = emptyMap<Any?, Any?>(),
                completionHandler = { success ->
                    println("Settings opened: $success")
                }
            )
        }
    }

    actual suspend fun requestPermission(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            // First check current status
            center.getNotificationSettingsWithCompletionHandler { settings ->
                val currentStatus = settings?.authorizationStatus

                when (currentStatus) {
                    UNAuthorizationStatusAuthorized,
                    UNAuthorizationStatusProvisional -> {
                        cachedPermissionState = true
                        println("✅ iOS Permission already granted")
                        continuation.resume(true)
                    }
                    UNAuthorizationStatusDenied -> {
                        cachedPermissionState = false
                        println("❌ iOS Permission denied")
                        continuation.resume(false)
                    }
                    UNAuthorizationStatusNotDetermined -> {
                        // Request permission
                        val options = UNAuthorizationOptionAlert or
                                UNAuthorizationOptionSound or
                                UNAuthorizationOptionBadge

                        println("🔔 Requesting iOS notification permission...")
                        center.requestAuthorizationWithOptions(
                            options = options,
                            completionHandler = { granted, error ->
                                if (error != null) {
                                    println("❌ iOS Permission error: ${error.localizedDescription}")
                                    cachedPermissionState = false
                                    continuation.resume(false)
                                } else {
                                    cachedPermissionState = granted
                                    println("✅ iOS Permission result: $granted")
                                    continuation.resume(granted)
                                }
                            }
                        )
                    }
                    else -> {
                        cachedPermissionState = false
                        continuation.resume(false)
                    }
                }
            }
        }
    }

    actual fun showNotification(title: String, body: String) {
        println("📱 iOS showNotification called: $title")

        // Always try to update and show
        updatePermissionCache { granted ->
            if (granted) {
                showNotificationInternal(title, body)
            } else {
                println("❌ Cannot show notification: Permission not granted")
            }
        }
    }

    private fun showNotificationInternal(title: String, body: String) {
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(UNNotificationSound.defaultSound)
            // Set badge to show notification worked
            setBadge(1 as NSNumber)
        }

        // Show after 1 second delay (notifications don't show when app is in foreground by default)
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            timeInterval = 5.0, // 5 second delay
            repeats = false
        )

        val id = "notif_${NSDate().timeIntervalSince1970}"
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = id,
            content = content,
            trigger = trigger
        )

        println("📬 Adding notification request with ID: $id")
        center.addNotificationRequest(request) { error ->
            if (error != null) {
                println("❌ Failed to show notification: ${error.localizedDescription}")
            } else {
                println("✅ iOS Notification scheduled successfully!")
                println("💡 Put app in background to see the notification")
            }
        }
    }
}