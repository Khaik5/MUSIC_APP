package com.example.musicapp.ui.auth.register

data class RegisterState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false
)
