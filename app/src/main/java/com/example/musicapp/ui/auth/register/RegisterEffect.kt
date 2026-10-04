package com.example.musicapp.ui.auth.register

sealed class RegisterEffect {
    data object RegisterSuccess: RegisterEffect()
    data class ShowError(val message: String) : RegisterEffect()
}
