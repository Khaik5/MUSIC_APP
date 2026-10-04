package com.example.musicapp.ui

sealed interface SplashEvent {
    data object Start: SplashEvent
}