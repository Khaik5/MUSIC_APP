package com.example.musicapp.ui.onboarding

sealed interface OnBoardingEvent {
    data object GetStartedClicked : OnBoardingEvent
}
