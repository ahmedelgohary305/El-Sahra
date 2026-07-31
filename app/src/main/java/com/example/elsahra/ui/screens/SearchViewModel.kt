package com.example.elsahra.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elsahra.data.model.Genre
import com.example.elsahra.data.model.Movie
import com.example.elsahra.data.repository.MovieRepository
import com.example.elsahra.data.repository.SearchHistoryRepository
import com.example.elsahra.util.ErrorMapper
import com.example.elsahra.util.LocaleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

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

    private val _isInitialLoading = MutableStateFlow(false)
    val isInitialLoading: StateFlow<Boolean> = _isInitialLoading

    private val _error = MutableStateFlow<Int?>(null)
    val error: StateFlow<Int?> = _error

    private val _initialLoadError = MutableStateFlow<Int?>(null)
    val initialLoadError: StateFlow<Int?> = _initialLoadError

    val recentSearches: StateFlow<List<String>> = historyRepository.recentSearches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredResults = combine(_searchResults, _selectedGenre) { results, genreId ->
        if (genreId == null) results
        else results.filter { it.genres?.any { genre -> genre.id == genreId } == true }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var searchJob: Job? = null
    private var latestSearchId = 0

    fun loadInitialData() {
        val language = LocaleManager.tmdbLanguageCode()

        viewModelScope.launch {
            _isInitialLoading.value = true
            _initialLoadError.value = null
            try {
                // Fetch genres and trending movies (as "Top Searches")
                _genres.value = movieRepository.getGenres(language)
                _topSearches.value = movieRepository.getTrendingMovies(language)
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    e.printStackTrace()
                    _initialLoadError.value = ErrorMapper.mapThrowableToStringRes(e)
                }
            } finally {
                _isInitialLoading.value = false
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
            delay(500.milliseconds) // Debounce
            search(query, searchId)
        }
    }

    private suspend fun search(query: String, searchId: Int) {
        val language = LocaleManager.tmdbLanguageCode()
        val region = LocaleManager.tmdbRegionCode()

        try {
            val results = movieRepository.searchMovies(query, language, region)
            if (searchId == latestSearchId) {
                _searchResults.value = results
                _error.value = null
            }
        } catch (e: Exception) {
            if (searchId == latestSearchId && e !is kotlinx.coroutines.CancellationException) {
                e.printStackTrace()
                _error.value = ErrorMapper.mapThrowableToStringRes(e)
            }
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
        
        // Only trigger a new search if the query has changed or if there are no results yet.
        // This prevents the search list from refreshing/flickering when clicking on a movie
        // or clicking search with the same query.
        if (query != _searchQuery.value || _searchResults.value.isEmpty()) {
            onQueryChanged(query)
        }
        
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
