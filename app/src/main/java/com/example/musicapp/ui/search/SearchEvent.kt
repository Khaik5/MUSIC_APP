package com.example.musicapp.ui.search

sealed interface SearchEvent {
    data object Load : SearchEvent
    data class QueryChanged(val query: String) : SearchEvent
    data class SongClicked(val songId: String) : SearchEvent
    data object CancelClicked : SearchEvent
}
