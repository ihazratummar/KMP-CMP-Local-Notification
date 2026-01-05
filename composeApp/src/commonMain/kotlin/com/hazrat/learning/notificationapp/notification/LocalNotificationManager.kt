package com.hazrat.learning.notificationapp.notification


/**
 * @author hazratummar
 * Created on 03/01/26
 */
 
expect class LocalNotificationManager {

    fun hasPermission() : Boolean

    suspend fun requestPermission() : Boolean

    fun openAppSettings()

    fun showNotification(title: String, body : String)

}