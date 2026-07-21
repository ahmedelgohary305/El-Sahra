package com.example.elsahra.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elsahra.data.model.Cast
import com.example.elsahra.data.model.Episode
import com.example.elsahra.data.model.Movie
import com.example.elsahra.data.model.MovieDetails
import com.example.elsahra.data.model.Review
import com.example.elsahra.data.model.TvShowDetails
import com.example.elsahra.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private var loadJob: Job? = null

    private val _movie = MutableStateFlow<MovieDetails?>(null)
    val movie: StateFlow<MovieDetails?> = _movie

    private val _tvShow = MutableStateFlow<TvShowDetails?>(null)
    val tvShow: StateFlow<TvShowDetails?> = _tvShow

    private val _cast = MutableStateFlow<List<Cast>>(emptyList())
    val cast: StateFlow<List<Cast>> = _cast

    private val _crew = MutableStateFlow<List<com.example.elsahra.data.model.Crew>>(emptyList())
    val crew: StateFlow<List<com.example.elsahra.data.model.Crew>> = _crew

    private val _recommendations = MutableStateFlow<List<Movie>>(emptyList())
    val recommendations: StateFlow<List<Movie>> = _recommendations

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews

    private val _episodes = MutableStateFlow<List<Episode>>(emptyList())
    val episodes: StateFlow<List<Episode>> = _episodes

    private val _trailerKey = MutableStateFlow<String?>(null)
    val trailerKey: StateFlow<String?> = _trailerKey

    private val _activeTmdbId = MutableStateFlow<Int?>(null)
    val activeTmdbId: StateFlow<Int?> = _activeTmdbId

    private val _currentSeason = MutableStateFlow<Int?>(null)
    val currentSeason: StateFlow<Int?> = _currentSeason

    private val _currentEpisode = MutableStateFlow<Int?>(null)
    val currentEpisode: StateFlow<Int?> = _currentEpisode

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadDetails(movieId: Int, mediaType: String? = "movie") {
        val locale = Locale.getDefault()
        val language = if (locale.language == "ar") "ar-EG" else "en-US"

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _movie.value = null
            _tvShow.value = null
            _cast.value = emptyList()
            _crew.value = emptyList()
            _recommendations.value = emptyList()
            _reviews.value = emptyList()
            _episodes.value = emptyList()
            _trailerKey.value = null

            try {
                if (mediaType == "tv") {
                    val tv = repository.getTvDetails(movieId, language)
                    val filteredSeasons = tv.seasons?.filter { it.seasonNumber > 0 }
                    val filteredTv = tv.copy(
                        seasons = filteredSeasons,
                        numberOfSeasons = filteredSeasons?.size ?: tv.numberOfSeasons,
                        numberOfEpisodes = filteredSeasons?.sumOf { it.episodeCount } ?: tv.numberOfEpisodes
                    )
                    _tvShow.value = filteredTv
                    val credits = repository.getTvCredits(movieId, language)
                    _cast.value = credits.cast
                    _crew.value = credits.crew
                    _recommendations.value = repository.getTvRecommendations(movieId, language)
                    _reviews.value = repository.getTvReviews(movieId, language)
                    
                    val videos = repository.getTvVideos(movieId, language)
                    _trailerKey.value = videos.find { it.type == "Trailer" && it.site == "YouTube" }?.key 
                        ?: videos.firstOrNull { it.site == "YouTube" }?.key

                    if (!filteredTv.seasons.isNullOrEmpty()) {
                        loadEpisodes(movieId, filteredTv.seasons[0].seasonNumber)
                    }
                } else {
                    _movie.value = repository.getMovieDetails(movieId, language)
                    val credits = repository.getMovieCredits(movieId, language)
                    _cast.value = credits.cast
                    _crew.value = credits.crew
                    _recommendations.value = repository.getMovieRecommendations(movieId, language)
                    _reviews.value = repository.getMovieReviews(movieId, language)
                    
                    val videos = repository.getMovieVideos(movieId, language)
                    _trailerKey.value = videos.find { it.type == "Trailer" && it.site == "YouTube" }?.key 
                        ?: videos.firstOrNull { it.site == "YouTube" }?.key
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    e.printStackTrace()
                    _error.value = e.message ?: "Unknown Error"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadEpisodes(tvId: Int, seasonNumber: Int) {
        val locale = Locale.getDefault()
        val language = if (locale.language == "ar") "ar-EG" else "en-US"
        viewModelScope.launch {
            try {
                _episodes.value = repository.getTvSeasonDetails(tvId, seasonNumber, language)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadMovieStream(tmdbId: Int) {
        _currentSeason.value = null
        _currentEpisode.value = null
        _activeTmdbId.value = tmdbId
    }

    fun loadTvStream(tmdbId: Int, season: Int, episode: Int) {
        _currentSeason.value = season
        _currentEpisode.value = episode
        _activeTmdbId.value = tmdbId
    }

    fun clearStream() {
        _activeTmdbId.value = null
    }
}
