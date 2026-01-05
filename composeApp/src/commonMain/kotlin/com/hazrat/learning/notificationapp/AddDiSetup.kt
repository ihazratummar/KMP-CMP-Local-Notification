package com.hazrat.learning.notificationapp

import com.hazrat.learning.notificationapp.notification.notificationModule
import com.hazrat.learning.notificationapp.notification.platformNotificationModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin


/**
 * @author hazratummar
 * Created on 03/01/26
 */

fun initKoin(koinApplication: ((KoinApplication) -> Unit)?= null) {
    startKoin {
        koinApplication?.invoke(this)
        modules(
            notificationModule,
            platformNotificationModule()
        )
    }
}