package com.example.musicapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val _state = MutableStateFlow(SplashState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<SplashEffect>()
    val effect = _effect.asSharedFlow()

    fun onEvent(event: SplashEvent) {
        when (event) {
            SplashEvent.Start -> startSplash()
        }
    }

    private fun startSplash() {
        viewModelScope.launch {
            _state.value = SplashState(loading = true)
            delay(2_000)
            _state.value = SplashState(loading = false)
            _effect.emit(
                if (authRepository.isLoggedIn()) {
                    SplashEffect.NavigationToMain
                } else {
                    SplashEffect.NavigationToOnBoarding
                }
            )
        }
    }
}
