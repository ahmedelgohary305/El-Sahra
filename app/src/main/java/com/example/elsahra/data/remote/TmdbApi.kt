package com.example.elsahra.data.remote

import com.example.elsahra.data.model.GenreResponse
import com.example.elsahra.data.model.MovieResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbApi {
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        @Query("region") region: String? = null
    ): MovieResponse

    @GET("tv/popular")
    suspend fun getPopularTvShows(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        @Query("region") region: String? = null
    ): MovieResponse

    @GET("tv/top_rated")
    suspend fun getTopRatedTvShows(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        @Query("region") region: String? = null
    ): MovieResponse

    @GET("tv/on_the_air")
    suspend fun getOnTheAirTvShows(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("trending/movie/day")
    suspend fun getTrendingMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("trending/tv/day")
    suspend fun getTrendingTvShows(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("search/multi")
    suspend fun searchMulti(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        @Query("region") region: String? = null
    ): MovieResponse

    @GET("genre/movie/list")
    suspend fun getMovieGenres(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): GenreResponse

    @GET("genre/tv/list")
    suspend fun getTvGenres(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): GenreResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @retrofit2.http.Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): com.example.elsahra.data.model.MovieDetails

    @GET("movie/{movie_id}/credits")
    suspend fun getMovieCredits(
        @retrofit2.http.Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): com.example.elsahra.data.model.CreditsResponse

    @GET("movie/{movie_id}/recommendations")
    suspend fun getMovieRecommendations(
        @retrofit2.http.Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("movie/{movie_id}/reviews")
    suspend fun getMovieReviews(
        @retrofit2.http.Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): com.example.elsahra.data.model.ReviewResponse

    @GET("tv/{tv_id}")
    suspend fun getTvDetails(
        @retrofit2.http.Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): com.example.elsahra.data.model.TvShowDetails

    @GET("tv/{tv_id}/credits")
    suspend fun getTvCredits(
        @retrofit2.http.Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): com.example.elsahra.data.model.CreditsResponse

    @GET("tv/{tv_id}/recommendations")
    suspend fun getTvRecommendations(
        @retrofit2.http.Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("tv/{tv_id}/reviews")
    suspend fun getTvReviews(
        @retrofit2.http.Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): com.example.elsahra.data.model.ReviewResponse

    @GET("tv/{tv_id}/season/{season_number}")
    suspend fun getTvSeasonDetails(
        @retrofit2.http.Path("tv_id") tvId: Int,
        @retrofit2.http.Path("season_number") seasonNumber: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): com.example.elsahra.data.model.SeasonDetails

    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @retrofit2.http.Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): com.example.elsahra.data.model.VideoResponse

    @GET("tv/{tv_id}/videos")
    suspend fun getTvVideos(
        @retrofit2.http.Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): com.example.elsahra.data.model.VideoResponse

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        @Query("with_genres") withGenres: String? = null,
        @Query("sort_by") sortBy: String? = "popularity.desc",
        @Query("region") region: String? = null,
        @Query("with_original_language") withOriginalLanguage: String? = null,
        @Query("vote_count.gte") voteCountGte: Int? = null,
        @Query("primary_release_date.gte") releaseDateGte: String? = null,
        @Query("primary_release_date.lte") releaseDateLte: String? = null
    ): MovieResponse

    @GET("discover/tv")
    suspend fun discoverTvShows(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        @Query("with_genres") withGenres: String? = null,
        @Query("sort_by") sortBy: String? = "popularity.desc",
        @Query("with_original_language") withOriginalLanguage: String? = null,
        @Query("vote_count.gte") voteCountGte: Int? = null,
        @Query("air_date.gte") airDateGte: String? = null,
        @Query("air_date.lte") airDateLte: String? = null,
        @Query("first_air_date.gte") firstAirDateGte: String? = null,
        @Query("first_air_date.lte") firstAirDateLte: String? = null
    ): MovieResponse
}
