package com.example.musicapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.repository.AuthRepository
import com.example.musicapp.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<HomeEffect>()
    val effect = _effect.asSharedFlow()

    init {
        onEvent(HomeEvent.Load)
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.Load -> _state.update {
                it.copy(
                    featured = MusicRepository.getFeaturedAlbums(),
                    trending = MusicRepository.getTrendingSongs(),
                    currentSong = it.currentSong ?: MusicRepository.getSong("blinding_lights")
                )
            }

            HomeEvent.SearchClicked -> emit(HomeEffect.NavigateToSearch)

            HomeEvent.LogoutClicked -> {
                authRepository.logout()
                emit(HomeEffect.NavigateToLogin)
            }

            is HomeEvent.SongClicked -> {
                _state.update {
                    it.copy(
                        currentSong = MusicRepository.getSong(event.songId),
                        isPlaying = true
                    )
                }
                viewModelScope.launch {
                    _effect.emit(HomeEffect.StartPlayback(event.songId))
                    _effect.emit(HomeEffect.NavigateToDetail(event.songId, autoPlay = false))
                }
            }

            is HomeEvent.DetailsClicked -> emit(
                HomeEffect.NavigateToDetail(event.songId, autoPlay = false)
            )

            HomeEvent.PlayPauseClicked -> {
                val current = _state.value.currentSong ?: return
                if (_state.value.activeServiceSongId == current.id) {
                    emit(HomeEffect.TogglePlayback)
                } else {
                    emit(HomeEffect.StartPlayback(current.id))
                }
            }

            is HomeEvent.ServiceSongChanged -> _state.update {
                it.copy(
                    currentSong = MusicRepository.getSong(event.songId),
                    activeServiceSongId = event.songId
                )
            }

            is HomeEvent.ServicePlayStateChanged -> _state.update {
                it.copy(isPlaying = event.isPlaying)
            }
        }
    }

    private fun emit(effect: HomeEffect) {
        viewModelScope.launch { _effect.emit(effect) }
    }
}
