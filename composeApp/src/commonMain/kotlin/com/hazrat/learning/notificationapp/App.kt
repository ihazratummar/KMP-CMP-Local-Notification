package com.hazrat.learning.notificationapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hazrat.learning.notificationapp.notification.NotificationHelper
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import notificationapp.composeapp.generated.resources.Res
import notificationapp.composeapp.generated.resources.compose_multiplatform
import org.koin.compose.koinInject

@Composable
fun App() {
    MaterialTheme {
        NotificationScreen()
    }
}

@Composable
fun NotificationScreen() {
    // Get the platform-specific implementation via Koin
    val notificationHelper: NotificationHelper = koinInject()

    // State management
    var title by remember { mutableStateOf("Reminder") }
    var body by remember { mutableStateOf("Don't forget to check your tasks!") }
    var showSuccessMessage by remember { mutableStateOf(false) }





    // Platform-specific permission handling
    NotificationPermissionHandler(
        notificationHelper = notificationHelper,
        onPermissionGranted = { /* Optional callback */ }
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "Notification Demo",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Test local notifications on your device",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Permission Toggle
            var isNotificationEnabled by remember { mutableStateOf(notificationHelper.checkPermission()) }

            // Poll for permission changes (e.g. when returning from settings)
            LaunchedEffect(Unit) {
                while(true) {
                    val current = notificationHelper.checkPermission()
                    if (current != isNotificationEnabled) {
                        isNotificationEnabled = current
                    }
                    kotlinx.coroutines.delay(1000) // Check every second
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Notifications",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isNotificationEnabled) "Notifications are on" else "Notifications are off",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = isNotificationEnabled,
                        onCheckedChange = { shouldEnable ->
                            if (shouldEnable) {
                                notificationHelper.requestPermissionAsync { granted ->
                                    if (granted){
                                        isNotificationEnabled = granted
                                    }else{
                                        notificationHelper.openAppSettings()
                                    }
                                }
                            } else {
                                // Open settings to let user disable it
                                notificationHelper.openAppSettings()
                            }
                        }
                    )
                }
            }

            // Input fields
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Notification Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Notification Body") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // Send button
            Button(
                onClick = {
                    notificationHelper.sendNotificationSafe(
                        title = title,
                        body = body,
                        onPermissionDenied = {
                            println("Permission denied - cannot send notification")
                        }
                    )
                    showSuccessMessage = true
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && body.isNotBlank()
            ) {
                Text("Send Notification")
            }

            // Success message
            if (showSuccessMessage) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✓",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Notification sent successfully!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(3000)
                    showSuccessMessage = false
                }
            }
        }
    }
}
