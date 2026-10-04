package com.example.musicapp.ui.search

import com.example.musicapp.data.model.Music

data class SearchState(
    val query: String = "",
    val results: List<Music> = emptyList()
)
