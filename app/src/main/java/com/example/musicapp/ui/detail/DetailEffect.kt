package com.example.musicapp.ui.detail

sealed interface DetailEffect {
    data object Finish : DetailEffect
    data class ServiceCommand(
        val action: String,
        val songId: String? = null,
        val positionMs: Int? = null
    ) : DetailEffect
}
