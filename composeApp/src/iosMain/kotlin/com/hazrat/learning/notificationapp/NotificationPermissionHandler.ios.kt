package com.hazrat.learning.notificationapp

import androidx.compose.runtime.Composable
import com.hazrat.learning.notificationapp.notification.NotificationHelper

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private enum class PermissionStatus {
    Unknown, Granted, Denied
}


@Composable
actual fun NotificationPermissionHandler(
    notificationHelper: NotificationHelper,
    onPermissionGranted: () -> Unit
) {
    var permissionStatus by remember { mutableStateOf<PermissionStatus>(PermissionStatus.Unknown) }
    var isRequesting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Check permission on launch
        println("🔍 Checking iOS notification permission...")
        notificationHelper.requestPermissionAsync { granted ->
            println("📋 Permission result: $granted")
            permissionStatus = if (granted) {
                onPermissionGranted()
                PermissionStatus.Granted
            } else {
                PermissionStatus.Denied
            }
        }
    }

    when (permissionStatus) {
        PermissionStatus.Unknown -> {
            // Show loading state
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Checking notification permission...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        PermissionStatus.Denied -> {
            Column(modifier = Modifier.padding(16.dp)) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "⚠️",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Permission Required",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }

                        Text(
                            text = "Notifications are disabled. Enable them to receive alerts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )

                        Button(
                            onClick = {
                                isRequesting = true
                                notificationHelper.requestPermissionAsync { granted ->
                                    permissionStatus = if (granted) {
                                        onPermissionGranted()
                                        PermissionStatus.Granted
                                    } else {
                                        PermissionStatus.Denied
                                    }
                                    isRequesting = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            enabled = !isRequesting
                        ) {
                            if (isRequesting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onError
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(if (isRequesting) "Requesting..." else "Enable Notifications")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // iOS specific info
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "💡 iOS Notification Tips",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "• Put the app in background to see notifications\n" +
                                    "• Notifications appear in Notification Center\n" +
                                    "• Check Settings > Notifications if issues persist",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
        PermissionStatus.Granted -> {
            // Show success banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "✅",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Column {
                        Text(
                            text = "Notifications Enabled",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Put app in background to see notifications",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}
