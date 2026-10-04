package com.example.musicapp.ui.search

sealed interface SearchEffect {
    data class NavigateToDetail(val songId: String) : SearchEffect
    data object Close : SearchEffect
}
