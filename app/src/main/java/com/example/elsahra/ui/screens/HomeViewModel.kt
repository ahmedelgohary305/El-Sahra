package com.example.elsahra.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.example.elsahra.data.model.Movie
import com.example.elsahra.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _language = MutableStateFlow(getLanguageCode())
    private val _region = MutableStateFlow(getRegionCode())

    private val _trending = MutableStateFlow<List<Movie>>(emptyList())
    val trending: StateFlow<List<Movie>> = _trending

    @OptIn(ExperimentalCoroutinesApi::class)
    val trendingPaging: Flow<PagingData<Movie>> = combine(_language, _trending) { lang, trendingList ->
        lang to trendingList.take(3).map { it.id }
    }.flatMapLatest { (lang, excludedIds) ->
        repository.getTrendingMoviesPaging(lang).map { pagingData ->
            pagingData.filter { movie -> movie.id !in excludedIds }
        }
    }.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val popularPaging: Flow<PagingData<Movie>> = combine(_language, _region) { lang, reg ->
        lang to reg
    }.flatMapLatest { (lang, reg) ->
        repository.getPopularMoviesPaging(lang, reg)
    }.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val topRatedPaging: Flow<PagingData<Movie>> = combine(_language, _region) { lang, reg ->
        lang to reg
    }.flatMapLatest { (lang, reg) ->
        repository.getTopRatedMoviesPaging(lang, reg)
    }.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val nowPlayingPaging: Flow<PagingData<Movie>> = combine(_language, _region) { lang, reg ->
        lang to reg
    }.flatMapLatest { (lang, reg) ->
        repository.getNowPlayingMoviesPaging(lang, reg)
    }.cachedIn(viewModelScope)

    init {
        // Observe language changes to reload non-paging data
        viewModelScope.launch {
            _language.collect { lang ->
                try {
                    val trendingList = repository.getTrendingMovies(lang)
                    // Fetch full details for the first 3 to get genres for the HeroBanner
                    val detailedTrending = trendingList.take(3).map { movie ->
                        try {
                            val details = repository.getMovieDetails(movie.id, lang)
                            movie.copy(genres = details.genres)
                        } catch (e: Exception) {
                            movie
                        }
                    }
                    _trending.value = detailedTrending + trendingList.drop(3)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun loadData() {
        _language.value = getLanguageCode()
        _region.value = getRegionCode()
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
