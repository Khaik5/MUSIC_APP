package com.example.musicapp.ui.auth.register

sealed class RegisterEvent {
    data class FullNameChanged(val fullName: String): RegisterEvent()
    data class EmailChanged(val email: String): RegisterEvent()
    data class PasswordChanged(val password: String): RegisterEvent()
    data object RegisterClicked: RegisterEvent()
}