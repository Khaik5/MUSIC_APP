package com.example.musicapp.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val _state = MutableStateFlow(SearchState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<SearchEffect>()
    val effect = _effect.asSharedFlow()

    init {
        onEvent(SearchEvent.Load)
    }

    fun onEvent(event: SearchEvent) {
        when (event) {
            SearchEvent.Load -> _state.value = SearchState(results = MusicRepository.search(""))

            is SearchEvent.QueryChanged -> _state.update {
                it.copy(query = event.query, results = MusicRepository.search(event.query))
            }

            is SearchEvent.SongClicked -> viewModelScope.launch {
                _effect.emit(SearchEffect.NavigateToDetail(event.songId))
            }

            SearchEvent.CancelClicked -> viewModelScope.launch {
                _effect.emit(SearchEffect.Close)
            }
        }
    }
}
