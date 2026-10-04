package com.example.musicapp.ui.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    private val repository = AuthRepository()
    private val _state = MutableStateFlow(RegisterState())
    val state = _state.asStateFlow()
    private val _effect = MutableSharedFlow<RegisterEffect>()
    val effect = _effect.asSharedFlow()
    fun onEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.FullNameChanged -> {
                _state.update {
                    it.copy(fullName = event.fullName)
                }
            }
            is RegisterEvent.PasswordChanged -> {
                _state.update {
                    it.copy(password = event.password)
                }
            }
            is RegisterEvent.EmailChanged -> {
                _state.update {
                    it.copy(email = event.email)
                }
            }
            is RegisterEvent.RegisterClicked -> {
                handleRegister()
            }
        }
    }

    private fun handleRegister() {
        val fullName = _state.value.fullName.trim()
        val email = _state.value.email.trim()
        val password = _state.value.password.trim()
        if (fullName.isEmpty()) {
            sendError("Please enter your full name")
            return
        }
        if (email.isEmpty()) {
            sendError("Please enter your email address")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            sendError("Please enter a valid email address")
            return
        }
        if (password.isEmpty()) {
            sendError("Please enter your password")
            return
        }
        if (password.length < 6) {
            sendError("Password must contain at least 6 characters")
            return
        }
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true)
            }
            try {
                repository.register(email, password, fullName)
                _effect.emit(RegisterEffect.RegisterSuccess)
            } catch (error: Exception) {
                _effect.emit(RegisterEffect.ShowError(error.toRegisterMessage()))
            } finally {
                _state.update {
                    it.copy(isLoading = false)
                }
            }
        }

    }

    private fun sendError(message: String) {
        viewModelScope.launch {
            _effect.emit(RegisterEffect.ShowError(message))
        }
    }

    private fun Exception.toRegisterMessage(): String = when (this) {
        is FirebaseAuthUserCollisionException -> "This email address is already registered"
        is FirebaseAuthWeakPasswordException -> "Password must contain at least 6 characters"
        else -> message ?: "Unable to create the account. Please try again."
    }

}
