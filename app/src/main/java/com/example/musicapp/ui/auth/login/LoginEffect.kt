package com.example.musicapp.ui.auth.login

sealed interface LoginEffect {
    data object LoginSuccess : LoginEffect
    data class ShowError(val message: String) : LoginEffect
}
