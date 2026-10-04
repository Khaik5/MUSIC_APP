package com.example.musicapp.ui.home

import com.example.musicapp.data.model.FeaturedAlbum
import com.example.musicapp.data.model.Music

data class HomeState(
    val featured: List<FeaturedAlbum> = emptyList(),
    val trending: List<Music> = emptyList(),
    val currentSong: Music? = null,
    val activeServiceSongId: String? = null,
    val isPlaying: Boolean = false
)
