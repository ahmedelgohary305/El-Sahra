package com.example.elsahra.data.repository

import android.content.Context
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.elsahra.data.model.Movie
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.watchlistDataStore by preferencesDataStore(name = "watchlist")
private val WatchlistItemsKey = stringSetPreferencesKey("items")

@JsonClass(generateAdapter = true)
data class WatchlistItem(
    val id: Int,
    val mediaType: String,
    val originalTitle: String? = null,
    val name: String? = null,
    val overview: String? = null,
    val posterPath: String? = null,
    val voteAverage: Double? = null,
    val voteCount: Int? = null
) {
    val key: String get() = "$mediaType:$id"

    fun toMovie() = Movie(
        id = id,
        originalTitle = originalTitle,
        name = name,
        overview = overview,
        posterPath = posterPath,
        voteAverage = voteAverage,
        voteCount = voteCount,
        mediaType = mediaType
    )
}

@Singleton
class WatchlistRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    moshi: Moshi
) {
    private val adapter = moshi.adapter(WatchlistItem::class.java)

    val items: Flow<List<WatchlistItem>> = context.watchlistDataStore.data.map { preferences ->
        preferences[WatchlistItemsKey].orEmpty()
            .mapNotNull { encoded -> runCatching { adapter.fromJson(encoded) }.getOrNull() }
    }

    suspend fun toggle(item: WatchlistItem) {
        context.watchlistDataStore.edit { preferences ->
            val current = preferences[WatchlistItemsKey].orEmpty().toMutableSet()
            val existing = current.firstOrNull { encoded ->
                runCatching { adapter.fromJson(encoded)?.key == item.key }.getOrDefault(false)
            }
            if (existing == null) current += adapter.toJson(item) else current -= existing
            preferences[WatchlistItemsKey] = current
        }
    }
}
