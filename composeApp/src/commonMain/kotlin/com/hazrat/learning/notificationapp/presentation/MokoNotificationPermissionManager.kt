package com.hazrat.learning.notificationapp.presentation

import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.notifications.REMOTE_NOTIFICATION


/**
 * @author hazratummar
 * Created on 08/01/26
 */

interface NotificationPermissionManager {
    suspend fun isGranted(permission: Permission): Boolean
    suspend fun request(permission: Permission): Boolean
    fun openSettings()
}



class MokoNotificationPermissionManager(
    private val controller: PermissionsController
): NotificationPermissionManager {
    override suspend fun isGranted(permission: Permission): Boolean {
        return controller.isPermissionGranted(permission)
    }

    override suspend fun request(permission: Permission): Boolean {
        return try {
            controller.providePermission(permission)
            true
        }catch (_: Throwable){
            false
        }
    }

    override fun openSettings() {
        controller.openAppSettings()
    }


}