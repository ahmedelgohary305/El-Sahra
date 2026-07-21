package com.example.elsahra.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "search_history")

@Singleton
class SearchHistoryRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // We store as a single String with a delimiter for better order control.
    private val HISTORY_STRING_KEY = stringPreferencesKey("search_history_string")

    val recentSearches: Flow<List<String>> = context.dataStore.data
        .map { preferences ->
            val history = preferences[HISTORY_STRING_KEY] ?: ""
            if (history.isEmpty()) emptyList() else history.split("|")
        }

    suspend fun addSearch(query: String) {
        if (query.isBlank()) return
        context.dataStore.edit { preferences ->
            val currentHistory = preferences[HISTORY_STRING_KEY] ?: ""
            val list = currentHistory.split("|").filter { it.isNotBlank() }.toMutableList()
            
            list.remove(query)
            list.add(0, query)
            
            val limitedList = if (list.size > 10) list.take(10) else list
            preferences[HISTORY_STRING_KEY] = limitedList.joinToString("|")
        }
    }

    suspend fun removeSearch(query: String) {
        context.dataStore.edit { preferences ->
            val currentHistory = preferences[HISTORY_STRING_KEY] ?: ""
            val newList = currentHistory.split("|").filter { it != query && it.isNotBlank() }
            preferences[HISTORY_STRING_KEY] = newList.joinToString("|")
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { preferences ->
            preferences.remove(HISTORY_STRING_KEY)
        }
    }
}
