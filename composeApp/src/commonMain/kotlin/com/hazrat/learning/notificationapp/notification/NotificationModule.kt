package com.hazrat.learning.notificationapp.notification

import org.koin.core.module.Module
import org.koin.dsl.module


/**
 * @author hazratummar
 * Created on 03/01/26
 */
 
val notificationModule = module {
    single { NotificationHelper(get()) }
    single { platformNotificationModule() }
}

expect fun platformNotificationModule() : Module