package com.example.musicapp.ui.onboarding

sealed interface OnBoardingEffect {
    data object NavigateToLogin : OnBoardingEffect
}
