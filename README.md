# NotificationApp - Compose Multiplatform Notification Demo

This project is a practical demonstration of handling **Local Notifications** in a **Kotlin Multiplatform (KMP)** application using **Compose Multiplatform**. 

It serves as a learning resource and reference for implementing platform-specific notification logic (Android & iOS) while maintaining a shared UI and business logic layer.

## 🚀 Features

*   **Cross-Platform Local Notifications:** Trigger local notifications on both Android and iOS from a single shared codebase.
*   **Permission Management:** 
    *   Request notification permissions at runtime.
    *   **Manual Toggle Control:** A UI switch to enable/disable notifications.
    *   **Settings Redirection:** Automatically opens the device's App Settings if the user needs to manually enable/disable permissions (since apps cannot revoke permissions programmatically).
    *   **Real-time Polling:** The UI updates automatically when returning from Settings to reflect the new permission state.
*   **Customizable Content:** Input fields to test dynamic notification titles and body text.
*   **Shared UI:** 100% shared UI code written in Compose Multiplatform.

## 🛠 Tech Stack

*   **Language:** [Kotlin](https://kotlinlang.org/)
*   **UI Framework:** [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
*   **Dependency Injection:** [Koin](https://insert-koin.io/)
*   **Architecture:** MVVM / Clean Architecture principles (Separation of UI and Platform Services).

## 📂 Project Structure

The core logic demonstrates the **Expect/Actual** mechanism in KMP:

*   **`commonMain`**: 
    *   `App.kt`: The shared UI entry point.
    *   `LocalNotificationManager` (expect class): Defines the interface for notification operations.
    *   `NotificationHelper`: A wrapper class for managing permission flows safely.
*   **`androidMain`**:
    *   `LocalNotificationManager` (actual class): Uses `NotificationManagerCompat` and Android Intents to show notifications and open settings. Handles Android 13+ runtime permissions (`POST_NOTIFICATIONS`).
*   **`iosMain`**:
    *   `LocalNotificationManager` (actual class): Uses `UNUserNotificationCenter` to schedule notifications and `UIApplication` to open settings.

## 🏃‍♂️ How to Run

### Android
You can run the application directly from Android Studio or via the terminal:

```shell
./gradlew :composeApp:installDebug
```

### iOS
1.  Open the `iosApp` directory in Xcode.
2.  Select your target simulator or device.
3.  Run the application.

*Note: On the iOS Simulator, notifications might not appear in the foreground depending on the simulation settings. It is recommended to press the "Send Notification" button and immediately lock the screen or background the app to see the notification banner.*

## 📸 Usage

1.  **Launch the App.**
2.  **Enable Notifications:** Use the toggle switch. If permissions are missing, a system dialog will appear.
3.  **Customize:** Enter a Title and Body for your notification.
4.  **Send:** Click "Send Notification". 
5.  **Disable:** Turn the toggle off. The app will redirect you to system settings to manually revoke permissions.

## 📄 License

This project is for educational purposes. Feel free to use the code in your own projects.
# KMP-CMP-Local-Notification
