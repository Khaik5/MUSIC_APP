package com.example.musicapp.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.musicapp.databinding.ActivityOnBoardingBinding
import com.example.musicapp.ui.auth.login.LoginActivity
import com.example.musicapp.utils.applySystemBarsInsets
import kotlinx.coroutines.launch

class OnBoardingActivity : AppCompatActivity() {
    private val binding by lazy { ActivityOnBoardingBinding.inflate(layoutInflater) }
    private val viewModel: OnBoardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        binding.root.applySystemBarsInsets()

        binding.btnGetStarted.setOnClickListener {
            viewModel.onEvent(OnBoardingEvent.GetStartedClicked)
        }
        observeState()
        observeEffect()
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.btnGetStarted.isEnabled = state.ready
                    binding.btnGetStarted.alpha = if (state.ready) 1f else 0.6f
                }
            }
        }
    }

    private fun observeEffect() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        OnBoardingEffect.NavigateToLogin -> {
                            startActivity(Intent(this@OnBoardingActivity, LoginActivity::class.java))
                            finish()
                        }
                    }
                }
            }
        }
    }
}
