package com.hazrat.learning.notificationapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.koin.core.context.GlobalContext
import org.koin.java.KoinJavaComponent.inject

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val koin = GlobalContext.get()
        val localNotificationManager = koin.get<LocalNotificationManager>()


        val title = intent.getStringExtra("title") ?: return
        val body = intent.getStringExtra("body") ?: return


        localNotificationManager.showNotification(title = title, body = body)

    }
}