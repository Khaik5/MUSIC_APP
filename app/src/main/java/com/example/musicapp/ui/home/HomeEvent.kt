package com.example.musicapp.ui.home

sealed interface HomeEvent {
    data object Load : HomeEvent
    data object SearchClicked : HomeEvent
    data object LogoutClicked : HomeEvent
    data class SongClicked(val songId: String) : HomeEvent
    data class DetailsClicked(val songId: String) : HomeEvent
    data object PlayPauseClicked : HomeEvent
    data class ServiceSongChanged(val songId: String) : HomeEvent
    data class ServicePlayStateChanged(val isPlaying: Boolean) : HomeEvent
}
