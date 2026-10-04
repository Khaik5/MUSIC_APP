package com.example.musicapp.ui.detail

import com.example.musicapp.data.model.Music

data class DetailState(
    val song: Music? = null,
    val activeServiceSongId: String? = null,
    val isPlaying: Boolean = false,
    val positionMs: Int = 0,
    val durationMs: Int = 176_000
)
