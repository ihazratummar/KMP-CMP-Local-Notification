package com.hazrat.learning.notificationapp.notification

import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformNotificationModule(): Module = module{

    single { LocalNotificationManager() }

}