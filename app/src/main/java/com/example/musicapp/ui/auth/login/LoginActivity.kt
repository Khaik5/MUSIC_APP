package com.example.musicapp.ui.auth.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.musicapp.MainActivity
import com.example.musicapp.R
import com.example.musicapp.databinding.ActivityLoginBinding
import com.example.musicapp.ui.auth.register.RegisterActivity
import com.example.musicapp.utils.applySystemBarsInsets
import com.example.musicapp.utils.bindPasswordVisibility
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    private val viewModel: LoginViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        initListener()
        initCollect()
        binding.loginRoot.applySystemBarsInsets(bottom = false)
        WindowCompat.getInsetsController(window, binding.loginRoot).isAppearanceLightStatusBars = true
        window.navigationBarColor = getColor(R.color.black)

    }
    private fun initListener(){
        binding.apply {
            btnBack.setOnClickListener {
                finish()
            }
            btnSignIn.setOnClickListener {
                viewModel.onEvent(LoginEvent.LoginClicked)
            }
            etUsername.doAfterTextChanged {
                viewModel.onEvent(LoginEvent.EmailChanged(it?.toString().orEmpty()))
            }
            etPassword.doAfterTextChanged {
                viewModel.onEvent(LoginEvent.PasswordChanged(it?.toString().orEmpty()))
            }
            btnTogglePassword.bindPasswordVisibility(etPassword)
            tvRegisterNow.setOnClickListener {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            }
        }
    }
    private fun initCollect(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when(effect){
                        LoginEffect.LoginSuccess -> openMain()
                        is LoginEffect.ShowError -> {
                            Toast.makeText(this@LoginActivity, effect.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.btnSignIn.isEnabled = !state.isLoading
                    binding.btnSignIn.alpha = if (state.isLoading) 0.6f else 1f
                }
            }
        }
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }
}
