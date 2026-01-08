package com.hazrat.learning.notificationapp.notification


/**
 * @author hazratummar
 * Created on 03/01/26
 */
 
expect class LocalNotificationManager {

    fun showNotification(title: String, body : String)

    fun scheduleNotification(id: String, title: String, body: String, timestamp: Long)
    
    fun cancelNotification(id: String)
    
    // Optional: Platform specific retrieval if supported, otherwise Repos will manage logic
    // But for "industry grade" persistence, we might not use this.
    // Let's stick to Repository managing the "List" logic via local DB/Prefs, 
    // and Manager just interacting with system.
    // However, iOS can return pending requests. Android not easily.
    // So we will keep 'getScheduledNotifications' in Repository as a flow from DB, 
    // and this Manager strictly for System interaction.
}