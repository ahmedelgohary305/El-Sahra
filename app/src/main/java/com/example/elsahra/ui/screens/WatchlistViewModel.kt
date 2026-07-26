package com.example.elsahra.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elsahra.data.model.Movie
import com.example.elsahra.data.repository.WatchlistItem
import com.example.elsahra.data.repository.WatchlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val repository: WatchlistRepository
) : ViewModel() {
    val items: StateFlow<List<WatchlistItem>> = repository.items.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    val movieIds: StateFlow<Set<Int>> = repository.items.map { items ->
        items.filter { it.mediaType == "movie" }.mapTo(mutableSetOf()) { it.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())
    val tvIds: StateFlow<Set<Int>> = repository.items.map { items ->
        items.filter { it.mediaType == "tv" }.mapTo(mutableSetOf()) { it.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun toggle(movie: Movie, fallbackMediaType: String = "movie") {
        viewModelScope.launch {
            repository.toggle(
                WatchlistItem(
                    id = movie.id,
                    mediaType = movie.mediaType ?: fallbackMediaType,
                    originalTitle = movie.originalTitle,
                    name = movie.name,
                    overview = movie.overview,
                    posterPath = movie.posterPath,
                    voteAverage = movie.voteAverage,
                    voteCount = movie.voteCount
                )
            )
        }
    }
}
