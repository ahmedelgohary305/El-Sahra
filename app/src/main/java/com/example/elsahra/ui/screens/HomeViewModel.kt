package com.example.elsahra.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.example.elsahra.data.model.Movie
import com.example.elsahra.data.repository.MovieRepository
import com.example.elsahra.util.ErrorMapper
import com.example.elsahra.util.LocaleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    enum class MediaType {
        MOVIE, TV
    }

    private val _selectedMediaType = MutableStateFlow(MediaType.MOVIE)
    val selectedMediaType: StateFlow<MediaType> = _selectedMediaType

    private val _language = MutableStateFlow(LocaleManager.tmdbLanguageCode())
    private val _region = MutableStateFlow(LocaleManager.tmdbRegionCode())

    private val _trendingMovies = MutableStateFlow<List<Movie>>(emptyList())
    val trendingMovies: StateFlow<List<Movie>> = _trendingMovies

    private val _trendingTvShows = MutableStateFlow<List<Movie>>(emptyList())
    val trendingTvShows: StateFlow<List<Movie>> = _trendingTvShows

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<Int?>(null)
    val error: StateFlow<Int?> = _error

    private val _refreshSignal = MutableSharedFlow<Unit>(replay = 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val trendingMoviesPaging: Flow<PagingData<Movie>> = combine(_language, _refreshSignal.onStart { emit(Unit) }) { lang, _ -> lang }
        .flatMapLatest { lang ->
            _trendingMovies
                .map { it.take(3).map { movie -> movie.id } }
                .distinctUntilChanged()
                .flatMapLatest { excludedIds ->
                    repository.getTrendingMoviesPaging(lang).map { pagingData ->
                        pagingData.filter { movie -> movie.id !in excludedIds }
                    }
                }
        }.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val trendingTvShowsPaging: Flow<PagingData<Movie>> = combine(_language, _refreshSignal.onStart { emit(Unit) }) { lang, _ -> lang }
        .flatMapLatest { lang ->
            _trendingTvShows
                .map { it.take(3).map { movie -> movie.id } }
                .distinctUntilChanged()
                .flatMapLatest { excludedIds ->
                    repository.getTrendingTvShowsPaging(lang).map { pagingData ->
                        pagingData.filter { movie -> movie.id !in excludedIds }
                    }
                }
        }.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val popularMoviesPaging: Flow<PagingData<Movie>> = combine(_language, _region, _refreshSignal.onStart { emit(Unit) }) { lang, reg, _ ->
        lang to reg
    }.flatMapLatest { (lang, reg) ->
        repository.getPopularMoviesPaging(lang, reg)
    }.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val popularTvShowsPaging: Flow<PagingData<Movie>> = combine(_language, _refreshSignal.onStart { emit(Unit) }) { lang, _ -> lang }
        .flatMapLatest { lang ->
            repository.getPopularTvShowsPaging(lang)
        }.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val topRatedMoviesPaging: Flow<PagingData<Movie>> = combine(_language, _region, _refreshSignal.onStart { emit(Unit) }) { lang, reg, _ ->
        lang to reg
    }.flatMapLatest { (lang, reg) ->
        repository.getTopRatedMoviesPaging(lang, reg)
    }.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val topRatedTvShowsPaging: Flow<PagingData<Movie>> = combine(_language, _refreshSignal.onStart { emit(Unit) }) { lang, _ -> lang }
        .flatMapLatest { lang ->
            repository.getTopRatedTvShowsPaging(lang)
        }.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val nowPlayingMoviesPaging: Flow<PagingData<Movie>> = combine(_language, _region, _refreshSignal.onStart { emit(Unit) }) { lang, reg, _ ->
        lang to reg
    }.flatMapLatest { (lang, reg) ->
        repository.getNowPlayingMoviesPaging(lang, reg)
    }.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    val onTheAirTvShowsPaging: Flow<PagingData<Movie>> = combine(_language, _refreshSignal.onStart { emit(Unit) }) { lang, _ -> lang }
        .flatMapLatest { lang ->
            repository.getOnTheAirTvShowsPaging(lang)
        }.cachedIn(viewModelScope)

    init {
        // Observe language changes to reload non-paging data
        viewModelScope.launch {
            _language.collect { lang ->
                refreshTrending(lang)
            }
        }
    }

    private fun refreshTrending(lang: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Fetch Movie Trending
                val movieTrending = repository.getTrendingMovies(lang)
                _trendingMovies.value = movieTrending
                // Fetch TV Trending
                val tvTrending = repository.getTrendingTvShows(lang)
                _trendingTvShows.value = tvTrending

                // Async fetch details to avoid blocking and double trigger of paging
                launch {
                    val detailedMovies = movieTrending.take(3).map { movie ->
                        try {
                            val details = repository.getMovieDetails(movie.id, lang)
                            movie.copy(genres = details.genres)
                        } catch (_: Exception) { movie }
                    }
                    _trendingMovies.value = detailedMovies + movieTrending.drop(3)
                }

                launch {
                    val detailedTv = tvTrending.take(3).map { movie ->
                        try {
                            val details = repository.getTvDetails(movie.id, lang)
                            movie.copy(genres = details.genres)
                        } catch (_: Exception) { movie }
                    }
                    _trendingTvShows.value = detailedTv + tvTrending.drop(3)
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    e.printStackTrace()
                    _error.value = ErrorMapper.mapThrowableToStringRes(e)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun retry() {
        refreshTrending(_language.value)
        viewModelScope.launch {
            _refreshSignal.emit(Unit)
        }
    }

    fun setSelectedMediaType(mediaType: MediaType) {
        _selectedMediaType.value = mediaType
    }

    fun loadData() {
        _language.value = LocaleManager.tmdbLanguageCode()
        _region.value = LocaleManager.tmdbRegionCode()
    }
}
