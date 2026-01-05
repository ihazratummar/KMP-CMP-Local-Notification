package com.hazrat.learning.notificationapp

import androidx.compose.runtime.Composable
import com.hazrat.learning.notificationapp.notification.NotificationHelper


/**
 * @author hazratummar
 * Created on 04/01/26
 */
 
@Composable
expect fun NotificationPermissionHandler(
    notificationHelper: NotificationHelper,
    onPermissionGranted : () -> Unit
)