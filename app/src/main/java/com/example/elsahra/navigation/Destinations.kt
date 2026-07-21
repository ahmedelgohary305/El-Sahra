package com.example.elsahra.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination {
    @Serializable
    data object Home : Destination
    @Serializable
    data object Search : Destination
    @Serializable
    data class Details(val movieId: Int, val mediaType: String? = "movie") : Destination
    @Serializable
    data object Settings : Destination
    @Serializable
    data class SeeAll(val category: String, val title: String) : Destination
}
