package com.example.musicapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.musicapp.MainActivity
import com.example.musicapp.databinding.ActivitySplashBinding
import com.example.musicapp.ui.onboarding.OnBoardingActivity
import com.example.musicapp.utils.applySystemBarsInsets
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    private val binding by lazy { ActivitySplashBinding.inflate(layoutInflater) }
    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        binding.root.applySystemBarsInsets()
        observeState()
        observeEffect()
        viewModel.onEvent(SplashEvent.Start)
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect {}
            }
        }
    }

    private fun observeEffect() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        SplashEffect.NavigationToOnBoarding -> {
                            startActivity(Intent(this@SplashActivity, OnBoardingActivity::class.java))
                            finish()
                        }
                        SplashEffect.NavigationToMain -> {
                            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                            finish()
                        }
                    }
                }
            }
        }
    }
}
