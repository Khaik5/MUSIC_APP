package com.example.musicapp.ui.home

sealed interface HomeEffect {
    data object NavigateToSearch : HomeEffect
    data object NavigateToLogin : HomeEffect
    data class NavigateToDetail(val songId: String, val autoPlay: Boolean) : HomeEffect
    data class StartPlayback(val songId: String) : HomeEffect
    data object TogglePlayback : HomeEffect
}
