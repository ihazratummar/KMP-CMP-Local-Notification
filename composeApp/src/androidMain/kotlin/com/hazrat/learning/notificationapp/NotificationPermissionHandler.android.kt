package com.hazrat.learning.notificationapp

import androidx.compose.runtime.Composable
import com.hazrat.learning.notificationapp.notification.NotificationHelper

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun NotificationPermissionHandler(
    notificationHelper: NotificationHelper,
    onPermissionGranted: () -> Unit
) {
    // Only request permission on Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionState = rememberPermissionState(
            Manifest.permission.POST_NOTIFICATIONS
        ) { granted ->
            if (granted) {
                onPermissionGranted()
            }
        }

        var showRationale by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            // Auto-request permission on first launch
            if (!permissionState.status.isGranted) {
                kotlinx.coroutines.delay(500) // Small delay for better UX
                permissionState.launchPermissionRequest()
            } else {
                onPermissionGranted()
            }
        }

        // Show rationale dialog if needed
        if (permissionState.status.shouldShowRationale || showRationale) {
            AlertDialog(
                onDismissRequest = { showRationale = false },
                title = { Text("Notification Permission") },
                text = {
                    Text(
                        "This app needs notification permission to send you reminders and alerts. " +
                                "Please grant the permission in the next dialog."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showRationale = false
                            permissionState.launchPermissionRequest()
                        }
                    ) {
                        Text("Grant Permission")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRationale = false }) {
                        Text("Not Now")
                    }
                }
            )
        }

        // Show permission status banner
        if (!permissionState.status.isGranted) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "⚠️ Permission Required",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Notifications are disabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Button(
                        onClick = { permissionState.launchPermissionRequest() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Enable")
                    }
                }
            }
        }
    } else {
        // Below Android 13, check if notifications are enabled
        var isEnabled by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            isEnabled = notificationHelper.checkPermission()
            if (isEnabled) {
                onPermissionGranted()
            }
        }

        if (!isEnabled) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ℹ️ Notifications Disabled",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Please enable notifications in your device settings",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}