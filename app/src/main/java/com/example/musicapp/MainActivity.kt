package com.example.musicapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.musicapp.broadcast.MusicBroadcastReceiver
import com.example.musicapp.data.model.FeaturedAlbum
import com.example.musicapp.data.model.Music
import com.example.musicapp.databinding.ActivityMainBinding
import com.example.musicapp.databinding.ItemFeaturedBinding
import com.example.musicapp.databinding.ItemSongBinding
import com.example.musicapp.service.MusicService
import com.example.musicapp.ui.detail.DetailActivity
import com.example.musicapp.ui.home.HomeEffect
import com.example.musicapp.ui.home.HomeEvent
import com.example.musicapp.ui.home.HomeState
import com.example.musicapp.ui.home.HomeViewModel
import com.example.musicapp.ui.auth.login.LoginActivity
import com.example.musicapp.ui.search.SearchActivity
import com.example.musicapp.utils.MusicConstants
import com.example.musicapp.utils.applySystemBarsInsets
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel: HomeViewModel by viewModels()
    private var receiverRegistered = false
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private val musicReceiver = MusicBroadcastReceiver(
        onSongChanged = { songId ->
            viewModel.onEvent(HomeEvent.ServiceSongChanged(songId))
        },
        onPlayStateChanged = { isPlaying ->
            viewModel.onEvent(HomeEvent.ServicePlayStateChanged(isPlaying))
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        binding.root.applySystemBarsInsets()
        requestNotificationPermission()
        setupClicks()
        setupBottomNavigation()
        observeState()
        observeEffect()
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter().apply {
            addAction(MusicConstants.ACTION_SONG_CHANGED)
            addAction(MusicConstants.ACTION_PLAY_STATE_CHANGED)
        }
        ContextCompat.registerReceiver(
            this, musicReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED
        )
        receiverRegistered = true
        startService(Intent(this, MusicService::class.java).apply {
            action = MusicConstants.ACTION_REQUEST_STATE
        })
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.selectedItemId = R.id.nav_home
    }

    override fun onStop() {
        if (receiverRegistered) {
            unregisterReceiver(musicReceiver)
            receiverRegistered = false
        }
        super.onStop()
    }

    private fun setupClicks() {
        binding.searchBar.setOnClickListener {
            viewModel.onEvent(HomeEvent.SearchClicked)
        }
        binding.btnMiniPlay.setOnClickListener {
            viewModel.onEvent(HomeEvent.PlayPauseClicked)
        }
        binding.miniPlayer.setOnClickListener {
            viewModel.state.value.currentSong?.let {
                viewModel.onEvent(HomeEvent.DetailsClicked(it.id))
            }
        }
    }

    private fun requestNotificationPermission() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_search -> {
                    viewModel.onEvent(HomeEvent.SearchClicked)
                    false
                }
                R.id.nav_profile -> {
                    showLogoutConfirmation()
                    false
                }
                R.id.nav_music, R.id.nav_radio -> false
                else -> false
            }
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect {
                    render(it)
                }
            }
        }
    }

    private fun observeEffect() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        HomeEffect.NavigateToSearch -> startActivity(
                            Intent(this@MainActivity, SearchActivity::class.java)
                        )
                        HomeEffect.NavigateToLogin -> openLogin()
                        is HomeEffect.NavigateToDetail -> openDetail(effect.songId, effect.autoPlay)
                        is HomeEffect.StartPlayback -> sendService(
                            MusicConstants.ACTION_PLAY_SONG, effect.songId
                        )
                        HomeEffect.TogglePlayback -> sendService(MusicConstants.ACTION_PLAY_PAUSE)
                    }
                }
            }
        }
    }

    private fun render(state: HomeState) {
        renderFeatured(state.featured)
        renderTrending(state.trending)
        renderMiniPlayer(state)
    }

    private fun renderFeatured(featured: List<FeaturedAlbum>) {
        binding.featuredContainer.removeAllViews()
        featured.forEach { album ->
            val item = ItemFeaturedBinding.inflate(
                LayoutInflater.from(this), binding.featuredContainer, false
            )
            item.ivCover.setImageResource(album.imageRes)
            item.tvTitle.text = album.title
            item.tvArtist.text = album.artist
            item.root.setOnClickListener {
                val songId = if (album.id == "starboy" || album.id == "anti") {
                    "blinding_lights"
                } else "90s_album"
                viewModel.onEvent(HomeEvent.DetailsClicked(songId))
            }
            binding.featuredContainer.addView(item.root)
        }
    }

    private fun renderTrending(songs: List<Music>) {
        binding.streamContainer.removeAllViews()
        songs.forEach { song ->
            val item = ItemSongBinding.inflate(
                LayoutInflater.from(this), binding.streamContainer, false
            )
            item.ivCover.setImageResource(song.imageRes)
            item.tvTitle.text = song.title
            item.tvArtist.text = song.artist
            item.tvMeta.text = song.meta
            item.ivLiveDot.visibility = if (song.id == "90s_album" || song.id == "cancer") {
                View.VISIBLE
            } else {
                View.GONE
            }
            item.root.setOnClickListener {
                viewModel.onEvent(HomeEvent.SongClicked(song.id))
            }
            item.btnMore.setOnClickListener {
                viewModel.onEvent(HomeEvent.DetailsClicked(song.id))
            }
            binding.streamContainer.addView(item.root)
        }
    }

    private fun renderMiniPlayer(state: HomeState) {
        val song = state.currentSong
        binding.miniPlayer.visibility = if (song == null) View.GONE else View.VISIBLE
        if (song == null) return
        binding.ivMiniCover.setImageResource(song.imageRes)
        binding.tvMiniTitle.text = song.title
        binding.tvMiniArtist.text = song.artist
        binding.btnMiniPlay.setImageResource(
            if (state.isPlaying){
                R.drawable.ic_pause_player
            }
            else {
                R.drawable.ic_play_mini
            }
        )
    }

    private fun openDetail(songId: String, autoPlay: Boolean) {
        startActivity(Intent(this, DetailActivity::class.java).apply {
            putExtra(MusicConstants.EXTRA_SONG_ID, songId)
            putExtra(MusicConstants.EXTRA_AUTO_PLAY, autoPlay)
        })
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.logout_title)
            .setMessage(R.string.logout_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.logout) { _, _ ->
                viewModel.onEvent(HomeEvent.LogoutClicked)
            }
            .show()
    }

    private fun openLogin() {
        stopService(Intent(this, MusicService::class.java))
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    private fun sendService(actionName: String, songId: String? = null) {
        val intent = Intent(this, MusicService::class.java).apply {
            action = actionName
            songId?.let {
                putExtra(MusicConstants.EXTRA_SONG_ID, it)
            }
        }
        ContextCompat.startForegroundService(this, intent)
    }
}
