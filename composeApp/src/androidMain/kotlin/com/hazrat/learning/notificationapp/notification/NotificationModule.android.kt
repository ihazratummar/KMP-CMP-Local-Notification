package com.hazrat.learning.notificationapp.notification

import androidx.activity.ComponentActivity
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformNotificationModule(): Module = module {
    single {
        val context = androidContext()
        if (context is ComponentActivity){
            LocalNotificationManager(context)
        }else {
            LocalNotificationManager(context)
        }
    }
}