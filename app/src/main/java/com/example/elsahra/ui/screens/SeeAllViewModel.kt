package com.example.elsahra.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.elsahra.data.model.Genre
import com.example.elsahra.data.model.Movie
import com.example.elsahra.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SeeAllViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _category = MutableStateFlow("")
    val category: StateFlow<String> = _category.asStateFlow()

    private val _selectedGenreId = MutableStateFlow<Int?>(null)
    val selectedGenreId: StateFlow<Int?> = _selectedGenreId.asStateFlow()

    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres.asStateFlow()

    private val _sortBy = MutableStateFlow("popularity.desc")
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val movies: Flow<PagingData<Movie>> = combine(_category, _selectedGenreId, _sortBy) { category, genreId, sortBy ->
        Triple(category, genreId, sortBy)
    }.flatMapLatest { (category, genreId, sortBy) ->
        val language = getLanguageCode()
        val region = getRegionCode()

        if (genreId != null) {
            repository.getMoviesByGenrePaging(genreId, language, sortBy, region)
        } else {
            // When in "All" tab (genreId == null)
            val isDefaultSortForCategory = when (category) {
                "popular" -> sortBy == "popularity.desc"
                "top_rated" -> sortBy == "vote_average.desc"
                "now_playing" -> sortBy == "primary_release_date.desc"
                "trending" -> sortBy == "popularity.desc" // Trending doesn't have a direct sortBy equivalent
                else -> true
            }

            if (isDefaultSortForCategory) {
                when (category) {
                    "popular" -> repository.getPopularMoviesPaging(language, region)
                    "top_rated" -> repository.getTopRatedMoviesPaging(language, region)
                    "now_playing" -> repository.getNowPlayingMoviesPaging(language, region)
                    "trending" -> repository.getTrendingMoviesPaging(language)
                    else -> emptyFlow()
                }
            } else {
                // User changed sort while in "All" tab, use discover
                repository.getMoviesByGenrePaging(null, language, sortBy, region)
            }
        }
    }.cachedIn(viewModelScope)

    init {
        fetchGenres()
    }

    private fun fetchGenres() {
        viewModelScope.launch {
            try {
                _genres.value = repository.getGenres(getLanguageCode())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setCategory(category: String) {
        _category.value = category
        _selectedGenreId.value = null
        // Set default sort based on category
        _sortBy.value = when (category) {
            "top_rated" -> "vote_average.desc"
            "now_playing" -> "primary_release_date.desc"
            else -> "popularity.desc"
        }
    }

    fun setSelectedGenre(genreId: Int?) {
        _selectedGenreId.value = genreId
    }

    fun setSortBy(sort: String) {
        _sortBy.value = sort
    }

    private fun getLanguageCode(): String {
        val locale = Locale.getDefault()
        return if (locale.language == "ar") "ar-EG" else "en-US"
    }

    private fun getRegionCode(): String? {
        val locale = Locale.getDefault()
        return if (locale.language == "ar") "EG" else if (locale.country.isNotBlank()) locale.country else null
    }
}
