package com.hazrat.learning.notificationapp.notification

import com.hazrat.learning.notificationapp.data.repository.NotificationRepositoryImpl
import com.hazrat.learning.notificationapp.domain.repository.NotificationRepository
import com.hazrat.learning.notificationapp.domain.usecase.CancelNotificationUseCase
import com.hazrat.learning.notificationapp.domain.usecase.GetScheduledNotificationsUseCase
import com.hazrat.learning.notificationapp.domain.usecase.ScheduleNotificationUseCase
import com.hazrat.learning.notificationapp.domain.usecase.SendNotificationUseCase
import com.hazrat.learning.notificationapp.presentation.MokoNotificationPermissionManager
import com.hazrat.learning.notificationapp.presentation.NotificationPermissionManager
import com.hazrat.learning.notificationapp.presentation.NotificationViewModel
import dev.icerock.moko.permissions.PermissionsController
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val notificationModule = module {
    
    // Repository
    single<NotificationRepository> { NotificationRepositoryImpl(get()) }
    
    // Use Cases
    factory { SendNotificationUseCase(get()) }
    factory { ScheduleNotificationUseCase(get()) }
    factory { CancelNotificationUseCase(get()) }
    factory { GetScheduledNotificationsUseCase(get()) }

    factory<NotificationPermissionManager>{params ->
        val controller = params.get<PermissionsController>()
        MokoNotificationPermissionManager(controller)
    }
    
    // ViewModel
    viewModel { params ->
        NotificationViewModel(
            sendNotificationUseCase = get(),
            scheduleNotificationUseCase = get(),
            cancelNotificationUseCase = get(),
            getScheduledNotificationsUseCase = get(),
            permissionManager = params.get()
        ) 
    }
}

expect fun platformNotificationModule() : Module