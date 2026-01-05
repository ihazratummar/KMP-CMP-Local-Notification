package com.hazrat.learning.notificationapp

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController (
    configure = {
        initKoin( )
    }
){ App() }