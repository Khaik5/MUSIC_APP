package com.example.musicapp.ui.auth.register

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
import com.example.musicapp.databinding.ActivityRegisterBinding
import com.example.musicapp.ui.auth.login.LoginActivity
import com.example.musicapp.utils.applySystemBarsInsets
import com.example.musicapp.utils.bindPasswordVisibility
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private val binding by lazy { ActivityRegisterBinding.inflate(layoutInflater) }
    private val viewModel: RegisterViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        initListener()
        initCollect()
        binding.registerRoot.applySystemBarsInsets(bottom = false)
        WindowCompat.getInsetsController(window, binding.registerRoot).isAppearanceLightStatusBars = true
        window.navigationBarColor = getColor(R.color.black)
    }
    private fun initListener(){
        binding.apply {
            btnBack.setOnClickListener {
                finish()
            }
            btnCreateAccount.setOnClickListener {
                viewModel.onEvent(RegisterEvent.RegisterClicked)
            }
            etFullName.doAfterTextChanged {
                viewModel.onEvent(RegisterEvent.FullNameChanged(it?.toString().orEmpty()))
            }
            etEmail.doAfterTextChanged {
                viewModel.onEvent(RegisterEvent.EmailChanged(it?.toString().orEmpty()))
            }
            etPassword.doAfterTextChanged {
                viewModel.onEvent(RegisterEvent.PasswordChanged(it?.toString().orEmpty()))
            }
            btnTogglePassword.bindPasswordVisibility(etPassword)
            tvSignIn.setOnClickListener {
                finish()
            }
        }
    }
    private fun initCollect(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when(effect){
                        RegisterEffect.RegisterSuccess -> openMain()
                        is RegisterEffect.ShowError -> {
                            Toast.makeText(this@RegisterActivity, effect.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.btnCreateAccount.isEnabled = !state.isLoading
                    binding.btnCreateAccount.alpha = if (state.isLoading) 0.6f else 1f
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
