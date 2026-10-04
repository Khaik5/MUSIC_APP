package com.example.musicapp.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.repository.MusicRepository
import com.example.musicapp.utils.MusicConstants
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailViewModel : ViewModel() {
    private val _state = MutableStateFlow(DetailState())
    val state = _state.asStateFlow()

    private val _effect = Channel<DetailEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: DetailEvent) {
        when (event) {
            is DetailEvent.Load -> {
                val song = MusicRepository.getSong(event.songId) ?: return
                _state.update { it.copy(song = song, durationMs = song.durationMs) }
                if (event.autoPlay) {
                    command(MusicConstants.ACTION_PLAY_SONG, songId = song.id)
                } else {
                    command(MusicConstants.ACTION_REQUEST_STATE)
                }
            }

            DetailEvent.BackClicked -> emit(DetailEffect.Finish)

            DetailEvent.PlayPauseClicked -> {
                val state = _state.value
                val song = state.song ?: return
                if (state.activeServiceSongId == song.id) {
                    command(MusicConstants.ACTION_PLAY_PAUSE)
                } else {
                    command(MusicConstants.ACTION_PLAY_SONG, songId = song.id)
                }
            }

            DetailEvent.NextClicked -> command(MusicConstants.ACTION_NEXT)
            DetailEvent.PreviousClicked -> command(MusicConstants.ACTION_PREVIOUS)

            is DetailEvent.SeekFinished -> {
                _state.update { it.copy(positionMs = event.positionMs) }
                command(MusicConstants.ACTION_SEEK, positionMs = event.positionMs)
            }

            is DetailEvent.ServiceSongChanged -> {
                val song = MusicRepository.getSong(event.songId) ?: return
                _state.update {
                    it.copy(
                        song = song,
                        activeServiceSongId = song.id,
                        durationMs = song.durationMs,
                        positionMs = 0
                    )
                }
            }

            is DetailEvent.ServicePlayStateChanged -> _state.update {
                it.copy(isPlaying = event.isPlaying)
            }

            is DetailEvent.ServiceProgressChanged -> _state.update {
                it.copy(
                    positionMs = event.positionMs,
                    durationMs = event.durationMs.takeIf { duration -> duration > 0 } ?: it.durationMs
                )
            }
        }
    }

    private fun command(action: String, songId: String? = null, positionMs: Int? = null) {
        emit(DetailEffect.ServiceCommand(action, songId, positionMs))
    }

    private fun emit(effect: DetailEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}
