package com.example.musicapp.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnBoardingViewModel : ViewModel() {
    private val _state = MutableStateFlow(OnBoardingState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<OnBoardingEffect>()
    val effect = _effect.asSharedFlow()

    fun onEvent(event: OnBoardingEvent) {
        when (event) {
            OnBoardingEvent.GetStartedClicked -> viewModelScope.launch {
                _effect.emit(OnBoardingEffect.NavigateToLogin)
            }
        }
    }
}
