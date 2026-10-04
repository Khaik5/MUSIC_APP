package com.example.musicapp.ui.detail

sealed interface DetailEvent {
    data class Load(val songId: String, val autoPlay: Boolean) : DetailEvent
    data object BackClicked : DetailEvent
    data object PlayPauseClicked : DetailEvent
    data object NextClicked : DetailEvent
    data object PreviousClicked : DetailEvent
    data class SeekFinished(val positionMs: Int) : DetailEvent
    data class ServiceSongChanged(val songId: String) : DetailEvent
    data class ServicePlayStateChanged(val isPlaying: Boolean) : DetailEvent
    data class ServiceProgressChanged(val positionMs: Int, val durationMs: Int) : DetailEvent
}
