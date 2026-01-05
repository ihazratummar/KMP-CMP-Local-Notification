package com.hazrat.learning.notificationapp.notification

import com.hazrat.learning.notificationapp.data.repository.NotificationRepositoryImpl
import com.hazrat.learning.notificationapp.domain.repository.NotificationRepository
import com.hazrat.learning.notificationapp.domain.usecase.ManagePermissionUseCase
import com.hazrat.learning.notificationapp.domain.usecase.SendNotificationUseCase
import com.hazrat.learning.notificationapp.presentation.NotificationViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val notificationModule = module {


    // Repository
    single<NotificationRepository> { NotificationRepositoryImpl(get()) }
    
    // Use Cases
    factory { SendNotificationUseCase(get()) }
    factory { ManagePermissionUseCase(get()) }
    
    // ViewModel
    viewModel { NotificationViewModel(get(), get()) }

}

expect fun platformNotificationModule() : Module