package com.hazrat.learning.notificationapp.notification

// The 'actual' keyword says: "Here's the iOS implementation"
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitSecond
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.Foundation.dateWithTimeIntervalSince1970
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
import platform.UserNotifications.UNCalendarNotificationTrigger
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
    // ... (existing code)

    actual fun scheduleNotification(id: String, title: String, body: String, timestamp: Long) {
        updatePermissionCache { granted ->
            if (granted) {
                // Convert timestamp (ms) to seconds
                val seconds = timestamp / 1000.0
                val date = NSDate.dateWithTimeIntervalSince1970(seconds)

                val triggerDate = NSCalendar.currentCalendar.components(
                    NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay or
                            NSCalendarUnitHour or NSCalendarUnitMinute or NSCalendarUnitSecond,
                    fromDate = date
                )

                val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
                    dateComponents = triggerDate,
                    repeats = false
                )

                val content = UNMutableNotificationContent().apply {
                    setTitle(title)
                    setBody(body)
                    setSound(UNNotificationSound.defaultSound)
                }

                val request = UNNotificationRequest.requestWithIdentifier(
                    identifier = id,
                    content = content,
                    trigger = trigger
                )

                center.addNotificationRequest(request) { error ->
                    if (error != null) {
                        println("❌ Failed to schedule notification: ${error.localizedDescription}")
                    } else {
                        println("✅ Notification scheduled for $date")
                    }
                }
            } else {
                println("❌ Cannot schedule notification: Permission not granted")
            }
        }
    }

    actual fun cancelNotification(id: String) {
        center.removePendingNotificationRequestsWithIdentifiers(listOf(id))
        println("🗑 iOS Notification cancelled: $id")
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