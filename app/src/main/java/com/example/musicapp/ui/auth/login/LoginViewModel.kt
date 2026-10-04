package com.example.musicapp.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel: ViewModel(){
    private val repository = AuthRepository()
    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()
    private val _effect = MutableSharedFlow<LoginEffect>()
    val effect = _effect.asSharedFlow()
    fun onEvent(event: LoginEvent){
        when(event){
            is LoginEvent.EmailChanged -> {
                _state.update {
                    it.copy(email = event.email)
                }
            }
            is LoginEvent.PasswordChanged -> {
                _state.update {
                    it.copy(password = event.password)
                }
            }
            is LoginEvent.LoginClicked -> {
                handleLogin()
            }

        }
    }
    private fun handleLogin() {
        val email = _state.value.email.trim()
        val password = _state.value.password
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
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true)
            }
            try {
                repository.login(email, password)
                _effect.emit(LoginEffect.LoginSuccess)
            } catch (error: Exception) {
                _effect.emit(LoginEffect.ShowError(error.toLoginMessage()))
            } finally {
                _state.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }
    private fun sendError(message: String){
        viewModelScope.launch {
            _effect.emit(LoginEffect.ShowError(message))
        }
    }

    private fun Exception.toLoginMessage(): String = when (this) {
        is FirebaseAuthInvalidUserException,
        is FirebaseAuthInvalidCredentialsException -> "Email or password is incorrect"
        else -> message ?: "Unable to sign in. Please try again."
    }
}
