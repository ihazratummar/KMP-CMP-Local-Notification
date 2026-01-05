package com.hazrat.learning.notificationapp.notification

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.definition.Callbacks


/**
 * @author hazratummar
 * Created on 03/01/26
 */

class NotificationHelper (
    private val manager: LocalNotificationManager
){

    fun checkPermission() : Boolean {
        return manager.hasPermission()
    }

    fun requestPermissionAsync(callbacks: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = manager.requestPermission()
            callbacks(result)
        }
    }

    fun openAppSettings() {
        manager.openAppSettings()
    }

    fun sendNotification(title: String, body: String) {
        // Try to send - the manager will handle permission checking
        manager.showNotification(title, body)
    }


    // Better approach: Check and request if needed, then send
    fun sendNotificationSafe(title: String, body: String, onPermissionDenied: () -> Unit = {}) {
        if (manager.hasPermission()) {
            manager.showNotification(title, body)
        } else {
            println("Permission not granted, requesting...")
            requestPermissionAsync { granted ->
                if (granted) {
                    manager.showNotification(title, body)
                } else {
                    onPermissionDenied()
                }
            }
        }
    }

}