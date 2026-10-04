package com.example.musicapp.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes

data class Music(
    val id: String,
    val title: String,
    val artist: String,
    val meta: String,
    @DrawableRes val imageRes: Int,
    @RawRes val audioRes: Int,
    val durationMs: Int = 176_000,
    val album: String = ""
)

data class FeaturedAlbum(
    val id: String,
    val title: String,
    val artist: String,
    @DrawableRes val imageRes: Int
)
