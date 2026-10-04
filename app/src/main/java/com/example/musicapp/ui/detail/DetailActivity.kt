package com.example.musicapp.ui.detail

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.musicapp.R
import com.example.musicapp.broadcast.MusicBroadcastReceiver
import com.example.musicapp.databinding.ActivityDetailBinding
import com.example.musicapp.service.MusicService
import com.example.musicapp.utils.MusicConstants
import com.example.musicapp.utils.applySystemBarsInsets
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {
    private val binding by lazy { ActivityDetailBinding.inflate(layoutInflater) }
    private val viewModel: DetailViewModel by viewModels()
    private var receiverRegistered = false

    private val musicReceiver = MusicBroadcastReceiver(
        onSongChanged = { songId ->
            viewModel.onEvent(DetailEvent.ServiceSongChanged(songId))
        },
        onPlayStateChanged = { isPlaying ->
            viewModel.onEvent(DetailEvent.ServicePlayStateChanged(isPlaying))
        },
        onProgressChanged = { positionMs, durationMs ->
            viewModel.onEvent(DetailEvent.ServiceProgressChanged(positionMs, durationMs))
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        binding.root.applySystemBarsInsets()

        setupClicks()
        observeState()
        observeEffect()

        val songId = intent.getStringExtra(MusicConstants.EXTRA_SONG_ID) ?: "blinding_lights"
        val autoPlay = intent.getBooleanExtra(MusicConstants.EXTRA_AUTO_PLAY, false)
        viewModel.onEvent(DetailEvent.Load(songId, autoPlay))
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter().apply {
            addAction(MusicConstants.ACTION_SONG_CHANGED)
            addAction(MusicConstants.ACTION_PLAY_STATE_CHANGED)
            addAction(MusicConstants.ACTION_PROGRESS_CHANGED)
        }
        ContextCompat.registerReceiver(
            this, musicReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED
        )
        receiverRegistered = true
    }

    override fun onStop() {
        if (receiverRegistered) {
            unregisterReceiver(musicReceiver)
            receiverRegistered = false
        }
        super.onStop()
    }

    private fun setupClicks() {
        binding.btnBack.setOnClickListener { viewModel.onEvent(DetailEvent.BackClicked) }
        binding.btnPlayPause.setOnClickListener { viewModel.onEvent(DetailEvent.PlayPauseClicked) }
        binding.btnNext.setOnClickListener { viewModel.onEvent(DetailEvent.NextClicked) }
        binding.btnPrevious.setOnClickListener { viewModel.onEvent(DetailEvent.PreviousClicked) }
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) = Unit
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                viewModel.onEvent(DetailEvent.SeekFinished(seekBar?.progress ?: 0))
            }
        })
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { render(it) }
            }
        }
    }

    private fun observeEffect() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        DetailEffect.Finish -> finish()
                        is DetailEffect.ServiceCommand -> sendServiceCommand(effect)
                    }
                }
            }
        }
    }

    private fun render(state: DetailState) {
        val song = state.song ?: return
        binding.ivBackground.setImageResource(
            if (song.id == "blinding_lights") R.drawable.art_blinding_lights else song.imageRes
        )
        binding.ivCover.setImageResource(
            if (song.id == "blinding_lights") R.drawable.art_blinding_lights else song.imageRes
        )
        binding.tvTitle.text = song.title
        binding.tvArtist.text = "${song.artist} - ${song.album.ifBlank { song.title }}"
        binding.btnPlayPause.setImageResource(
            if (state.isPlaying && state.activeServiceSongId == song.id) {
                R.drawable.ic_pause_player
            } else R.drawable.ic_play_player
        )
        binding.seekBar.max = state.durationMs.coerceAtLeast(1)
        if (!binding.seekBar.isPressed) binding.seekBar.progress = state.positionMs
        binding.tvCurrentTime.text = formatTime(state.positionMs)
        binding.tvRemainingTime.text = "-${formatTime((state.durationMs - state.positionMs).coerceAtLeast(0))}"
    }

    private fun sendServiceCommand(effect: DetailEffect.ServiceCommand) {
        val serviceIntent = Intent(this, MusicService::class.java).apply {
            action = effect.action
            effect.songId?.let { putExtra(MusicConstants.EXTRA_SONG_ID, it) }
            effect.positionMs?.let { putExtra(MusicConstants.EXTRA_POSITION, it) }
        }
        if (effect.action == MusicConstants.ACTION_REQUEST_STATE) {
            startService(serviceIntent)
        } else {
            ContextCompat.startForegroundService(this, serviceIntent)
        }
    }

    private fun formatTime(milliseconds: Int): String {
        val totalSeconds = milliseconds / 1000
        return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
    }
}
