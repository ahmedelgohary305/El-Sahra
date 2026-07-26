package com.example.elsahra.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elsahra.data.model.Genre
import com.example.elsahra.data.model.Movie
import com.example.elsahra.data.repository.MovieRepository
import com.example.elsahra.data.repository.SearchHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val historyRepository: SearchHistoryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults = MutableStateFlow<List<Movie>>(emptyList())
    
    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres

    private val _topSearches = MutableStateFlow<List<Movie>>(emptyList())
    val topSearches: StateFlow<List<Movie>> = _topSearches

    private val _selectedGenre = MutableStateFlow<Int?>(null)
    val selectedGenre: StateFlow<Int?> = _selectedGenre

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val recentSearches: StateFlow<List<String>> = historyRepository.recentSearches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredResults = combine(_searchResults, _selectedGenre) { results, genreId ->
        if (genreId == null) results
        else results.filter { it.genres?.any { genre -> genre.id == genreId } == true }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var searchJob: Job? = null
    private var latestSearchId = 0

    fun loadInitialData() {
        val locale = Locale.getDefault()
        val language = if (locale.language == "ar") "ar-EG" else "en-US"

        viewModelScope.launch {
            try {
                // Fetch genres and trending movies (as "Top Searches")
                _genres.value = movieRepository.getGenres(language)
                _topSearches.value = movieRepository.getTrendingMovies(language)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        val searchId = ++latestSearchId
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _isLoading.value = false
            return
        }

        // Show the loading UI during the debounce interval too; otherwise the
        // empty state flashes before the request starts.
        _isLoading.value = true
        searchJob = viewModelScope.launch {
            delay(500) // Debounce
            search(query, searchId)
        }
    }

    private suspend fun search(query: String, searchId: Int) {
        val locale = Locale.getDefault()
        val language = if (locale.language == "ar") "ar-EG" else "en-US"
        val region = if (locale.language == "ar") "EG" else if (locale.country.isNotBlank()) locale.country else null

        try {
            val results = movieRepository.searchMovies(query, language, region)
            if (searchId == latestSearchId) {
                _searchResults.value = results
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // A canceled request must not stop the loading indicator for the
            // newer query that replaced it.
            if (searchId == latestSearchId) {
                _isLoading.value = false
            }
        }
    }

    fun onSearchTriggered(query: String) {
        if (query.isBlank()) return
        onQueryChanged(query)
        viewModelScope.launch {
            historyRepository.addSearch(query)
        }
    }

    fun selectGenre(genreId: Int?) {
        _selectedGenre.value = genreId
    }

    fun removeRecentSearch(query: String) {
        viewModelScope.launch {
            historyRepository.removeSearch(query)
        }
    }

    fun clearAllRecentSearches() {
        viewModelScope.launch {
            historyRepository.clearHistory()
        }
    }
}
