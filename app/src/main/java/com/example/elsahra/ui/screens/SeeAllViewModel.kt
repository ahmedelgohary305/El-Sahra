package com.example.elsahra.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.elsahra.data.model.Genre
import com.example.elsahra.data.model.Movie
import com.example.elsahra.data.repository.MovieRepository
import com.example.elsahra.util.LocaleManager
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

    private val _mediaType = MutableStateFlow("movie")
    val mediaType: StateFlow<String> = _mediaType.asStateFlow()

    private val _selectedGenreId = MutableStateFlow<Int?>(null)
    val selectedGenreId: StateFlow<Int?> = _selectedGenreId.asStateFlow()

    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres.asStateFlow()

    private val _sortBy = MutableStateFlow("popularity.desc")
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val movies: Flow<PagingData<Movie>> = combine(_category, _mediaType, _selectedGenreId, _sortBy) { category, mediaType, genreId, sortBy ->
        DataParams(category, mediaType, genreId, sortBy)
    }.flatMapLatest { params ->
        val language = getLanguageCode()
        val region = getRegionCode()
        
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val calendar = java.util.Calendar.getInstance()
        val today = sdf.format(calendar.time)
        
        val calendarHelper = java.util.Calendar.getInstance()
        calendarHelper.add(java.util.Calendar.MONTH, -1)
        val oneMonthAgo = sdf.format(calendarHelper.time)
        
        calendarHelper.time = calendar.time
        calendarHelper.add(java.util.Calendar.DATE, 7)
        val nextWeek = sdf.format(calendarHelper.time)
        
        calendarHelper.time = calendar.time
        calendarHelper.add(java.util.Calendar.MONTH, -3)
        val threeMonthsAgo = sdf.format(calendarHelper.time)

        calendarHelper.time = calendar.time
        calendarHelper.add(java.util.Calendar.MONTH, -6)
        val sixMonthsAgo = sdf.format(calendarHelper.time)

        if (params.genreId != null) {
            // Quality filter: Use a minimum vote count when sorting by rating to avoid obscure entries
            val voteCountGte = when {
                params.sortBy == "vote_average.desc" -> if (params.mediaType == "movie") 500 else 100
                else -> 10 // Minimum threshold for genre filtering
            }
            
            if (params.mediaType == "movie") {
                val (dateGte, dateLte) = when (params.category) {
                    "now_playing" -> oneMonthAgo to today
                    "trending" -> threeMonthsAgo to today
                    else -> null to null
                }
                repository.getMoviesByGenrePaging(params.genreId, language, params.sortBy, region, voteCountGte, dateGte, dateLte)
            } else {
                // TV Shows specific date filters
                val (airGte, airLte, firstAirGte, firstAirLte) = when (params.category) {
                    "now_playing" -> {
                        // "On Air" for TV means episodes airing soon
                        Quad(today, nextWeek, null, null)
                    }
                    "trending" -> {
                        // "Trending" for TV - recent shows
                        Quad(null, null, sixMonthsAgo, today)
                    }
                    else -> Quad(null, null, null, null)
                }
                repository.getTvShowsByGenrePaging(
                    params.genreId, language, params.sortBy, voteCountGte,
                    airDateGte = airGte, airDateLte = airLte,
                    firstAirDateGte = firstAirGte, firstAirDateLte = firstAirLte
                )
            }
        } else {
            // When in "All" tab (genreId == null)
            val isDefaultSortForCategory = when (params.category) {
                "popular" -> params.sortBy == "popularity.desc"
                "top_rated" -> params.sortBy == "vote_average.desc"
                "now_playing" -> params.sortBy == "primary_release_date.desc"
                "trending" -> params.sortBy == "popularity.desc"
                else -> true
            }

            if (isDefaultSortForCategory) {
                if (params.mediaType == "movie") {
                    when (params.category) {
                        "popular" -> repository.getPopularMoviesPaging(language, region)
                        "top_rated" -> repository.getTopRatedMoviesPaging(language, region)
                        "now_playing" -> repository.getNowPlayingMoviesPaging(language, region)
                        "trending" -> repository.getTrendingMoviesPaging(language)
                        else -> emptyFlow()
                    }
                } else {
                    when (params.category) {
                        "popular" -> repository.getPopularTvShowsPaging(language)
                        "top_rated" -> repository.getTopRatedTvShowsPaging(language)
                        "now_playing" -> repository.getOnTheAirTvShowsPaging(language)
                        "trending" -> repository.getTrendingTvShowsPaging(language)
                        else -> emptyFlow()
                    }
                }
            } else {
                // User changed sort while in "All" tab, use discover
                val voteCountGte = if (params.sortBy == "vote_average.desc") 100 else 0
                if (params.mediaType == "movie") {
                    repository.getMoviesByGenrePaging(null, language, params.sortBy, region, voteCountGte)
                } else {
                    repository.getTvShowsByGenrePaging(null, language, params.sortBy, voteCountGte)
                }
            }
        }
    }.cachedIn(viewModelScope)

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    data class DataParams(val category: String, val mediaType: String, val genreId: Int?, val sortBy: String)

    init {
        fetchGenres()
    }

    private fun fetchGenres() {
        viewModelScope.launch {
            try {
                _genres.value = if (_mediaType.value == "movie") {
                    repository.getGenres(getLanguageCode())
                } else {
                    repository.getTvGenres(getLanguageCode())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setCategory(category: String, mediaType: String? = "movie") {
        _category.value = category
        _mediaType.value = mediaType ?: "movie"
        _selectedGenreId.value = null
        // Set default sort based on category
        _sortBy.value = when (category) {
            "top_rated" -> "vote_average.desc"
            "now_playing" -> "primary_release_date.desc"
            else -> "popularity.desc"
        }
        fetchGenres()
    }

    fun setSelectedGenre(genreId: Int?) {
        _selectedGenreId.value = genreId
    }

    fun setSortBy(sort: String) {
        _sortBy.value = sort
    }

    private fun getLanguageCode(): String {
        return LocaleManager.tmdbLanguageCode()
    }

    private fun getRegionCode(): String? {
        return LocaleManager.tmdbRegionCode()
    }
}
