package com.example.musicapp.ui

sealed interface SplashEffect {
    data object NavigationToOnBoarding : SplashEffect
    data object NavigationToMain : SplashEffect
}
