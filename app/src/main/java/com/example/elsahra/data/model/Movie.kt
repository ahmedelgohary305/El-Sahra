package com.example.elsahra.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MovieResponse(
    @Json(name = "results") val results: List<Movie>,
    @Json(name = "total_pages") val totalPages: Int
)

@JsonClass(generateAdapter = true)
data class Movie(
    @Json(name = "id") val id: Int,
    @Json(name = "original_title") val originalTitle: String?,
    @Json(name = "name") val name: String?, // For TV shows
    @Json(name = "overview") val overview: String?,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "vote_average") val voteAverage: Double?,
    @Json(name = "vote_count") val voteCount: Int?,
    @Json(name = "media_type") val mediaType: String?,
    @Json(name = "genres") val genres: List<Genre>? = emptyList()
) {
    val displayTitle: String get() = originalTitle ?: name ?: "Unknown"
    val fullPosterPath: String get() = "https://image.tmdb.org/t/p/w500$posterPath"
}

@JsonClass(generateAdapter = true)
data class MovieDetails(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String?,
    @Json(name = "original_title") val originalTitle: String?,
    @Json(name = "overview") val overview: String?,
    @Json(name = "backdrop_path") val backdropPath: String?,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "vote_average") val voteAverage: Double?,
    @Json(name = "vote_count") val voteCount: Int?,
    @Json(name = "release_date") val releaseDate: String?,
    @Json(name = "genres") val genres: List<Genre>?,
    @Json(name = "runtime") val runtime: Int?,
    @Json(name = "status") val status: String?,
    @Json(name = "tagline") val tagline: String?,
    @Json(name = "original_language") val originalLanguage: String?,
    @Json(name = "adult") val adult: Boolean? = false,
    @Json(name = "budget") val budget: Long? = 0,
    @Json(name = "revenue") val revenue: Long? = 0,
    @Json(name = "production_companies") val productionCompanies: List<ProductionCompany>? = emptyList()
) {
    val displayTitle: String get() = originalTitle ?: title ?: "Unknown"
    val fullBackdropPath: String get() = "https://image.tmdb.org/t/p/original$backdropPath"
}

@JsonClass(generateAdapter = true)
data class ProductionCompany(
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class GenreResponse(
    @Json(name = "genres") val genres: List<Genre>
)

@JsonClass(generateAdapter = true)
data class Genre(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class CreditsResponse(
    @Json(name = "cast") val cast: List<Cast>,
    @Json(name = "crew") val crew: List<Crew>
)

@JsonClass(generateAdapter = true)
data class Cast(
    @Json(name = "name") val name: String,
    @Json(name = "profile_path") val profilePath: String?,
    @Json(name = "character") val character: String
) {
    val fullProfilePath: String? get() = profilePath?.let { "https://image.tmdb.org/t/p/w185$it" }
}

@JsonClass(generateAdapter = true)
data class Crew(
    @Json(name = "name") val name: String,
    @Json(name = "job") val job: String,
    @Json(name = "department") val department: String
)

@JsonClass(generateAdapter = true)
data class ReviewResponse(
    @Json(name = "results") val results: List<Review>
)

@JsonClass(generateAdapter = true)
data class Review(
    @Json(name = "author") val author: String,
    @Json(name = "content") val content: String,
    @Json(name = "author_details") val authorDetails: AuthorDetails?
)

@JsonClass(generateAdapter = true)
data class AuthorDetails(
    @Json(name = "avatar_path") val avatarPath: String?,
    @Json(name = "rating") val rating: Double?
) {
    val fullAvatarPath: String? get() = avatarPath?.let { 
        if (it.startsWith("http")) it else "https://image.tmdb.org/t/p/w185$it"
    }
}

@JsonClass(generateAdapter = true)
data class TvShowDetails(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "overview") val overview: String?,
    @Json(name = "backdrop_path") val backdropPath: String?,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "vote_average") val voteAverage: Double?,
    @Json(name = "vote_count") val voteCount: Int?,
    @Json(name = "first_air_date") val firstAirDate: String?,
    @Json(name = "genres") val genres: List<Genre>?,
    @Json(name = "number_of_seasons") val numberOfSeasons: Int?,
    @Json(name = "number_of_episodes") val numberOfEpisodes: Int?,
    @Json(name = "seasons") val seasons: List<Season>?,
    @Json(name = "status") val status: String?,
    @Json(name = "tagline") val tagline: String?,
    @Json(name = "original_language") val originalLanguage: String?,
    @Json(name = "original_name") val originalName: String?,
    @Json(name = "adult") val adult: Boolean? = false,
    @Json(name = "production_companies") val productionCompanies: List<ProductionCompany>? = emptyList()
) {
    val fullBackdropPath: String get() = "https://image.tmdb.org/t/p/original$backdropPath"
}

@JsonClass(generateAdapter = true)
data class Season(
    @Json(name = "season_number") val seasonNumber: Int,
    @Json(name = "episode_count") val episodeCount: Int,
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class SeasonDetails(
    @Json(name = "episodes") val episodes: List<Episode>
)

@JsonClass(generateAdapter = true)
data class Episode(
    @Json(name = "name") val name: String,
    @Json(name = "overview") val overview: String?,
    @Json(name = "episode_number") val episodeNumber: Int,
    @Json(name = "season_number") val seasonNumber: Int,
    @Json(name = "still_path") val stillPath: String?,
    @Json(name = "runtime") val runtime: Int?
) {
    val fullStillPath: String get() = "https://image.tmdb.org/t/p/w300$stillPath"
}

@JsonClass(generateAdapter = true)
data class VideoResponse(
    @Json(name = "results") val results: List<Video>
)

@JsonClass(generateAdapter = true)
data class Video(
    @Json(name = "key") val key: String,
    @Json(name = "site") val site: String,
    @Json(name = "type") val type: String
)



