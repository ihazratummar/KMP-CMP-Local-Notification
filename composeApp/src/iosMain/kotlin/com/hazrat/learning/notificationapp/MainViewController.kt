package com.hazrat.learning.notificationapp

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController (
    configure = {
        // Koin is initialized in iOSApp.swift
        initKoin()
    }
){ App() }