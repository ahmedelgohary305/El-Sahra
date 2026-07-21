package com.example.elsahra.data.repository

import com.example.elsahra.BuildConfig
import com.example.elsahra.data.model.Genre
import com.example.elsahra.data.model.Movie
import com.example.elsahra.data.remote.TmdbApi
import com.example.elsahra.data.paging.MoviePagingSource
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MovieRepository @Inject constructor(private val api: TmdbApi) {
    private val apiKey = BuildConfig.TMDB_API_KEY

    fun getPopularMoviesPaging(language: String, region: String? = null): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { MoviePagingSource { page -> api.getPopularMovies(apiKey, language, page, region) } }
        ).flow
    }

    fun getTopRatedMoviesPaging(language: String, region: String? = null): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { MoviePagingSource { page -> api.getTopRatedMovies(apiKey, language, page, region) } }
        ).flow
    }

    fun getNowPlayingMoviesPaging(language: String, region: String? = null): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { MoviePagingSource { page -> api.getNowPlayingMovies(apiKey, language, page, region) } }
        ).flow
    }

    fun getTrendingMoviesPaging(language: String): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { MoviePagingSource { page -> api.getTrendingMovies(apiKey, language, page) } }
        ).flow
    }

    suspend fun getTrendingMovies(language: String): List<Movie> {
        return api.getTrendingMovies(apiKey, language).results
    }

    suspend fun searchMovies(query: String, language: String, region: String? = null): List<Movie> {
        return api.searchMulti(apiKey, query, language, region = region).results
    }

    suspend fun getGenres(language: String): List<Genre> {
        return api.getMovieGenres(apiKey, language).genres
    }

    suspend fun getMovieDetails(movieId: Int, language: String): com.example.elsahra.data.model.MovieDetails {
        return api.getMovieDetails(movieId, apiKey, language)
    }

    suspend fun getMovieCredits(movieId: Int, language: String) = 
        api.getMovieCredits(movieId, apiKey, language)

    suspend fun getMovieRecommendations(movieId: Int, language: String) =
        api.getMovieRecommendations(movieId, apiKey, language).results

    suspend fun getMovieReviews(movieId: Int, language: String) = 
        api.getMovieReviews(movieId, apiKey, language).results

    suspend fun getTvDetails(tvId: Int, language: String) =
        api.getTvDetails(tvId, apiKey, language)

    suspend fun getTvCredits(tvId: Int, language: String) = 
        api.getTvCredits(tvId, apiKey, language)

    suspend fun getTvRecommendations(tvId: Int, language: String) =
        api.getTvRecommendations(tvId, apiKey, language).results

    suspend fun getTvReviews(tvId: Int, language: String) = 
        api.getTvReviews(tvId, apiKey, language).results

    suspend fun getTvSeasonDetails(tvId: Int, seasonNumber: Int, language: String) = 
        api.getTvSeasonDetails(tvId, seasonNumber, apiKey, language).episodes

    suspend fun getMovieVideos(movieId: Int, language: String) =
        api.getMovieVideos(movieId, apiKey, language).results

    suspend fun getTvVideos(tvId: Int, language: String) =
        api.getTvVideos(tvId, apiKey, language).results

    fun getMoviesByGenrePaging(genreId: Int?, language: String, sortBy: String = "popularity.desc", region: String? = null): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { MoviePagingSource { page -> api.discoverMovies(apiKey, language, page, genreId?.toString(), sortBy, region) } }
        ).flow
    }
}
