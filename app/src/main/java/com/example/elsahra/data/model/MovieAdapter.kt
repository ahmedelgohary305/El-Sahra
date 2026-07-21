package com.example.elsahra.data.model

import com.squareup.moshi.*

class MovieAdapter {
    @FromJson
    fun fromJson(reader: JsonReader, delegate: JsonAdapter<Movie>): Movie? {
        val json = reader.readJsonValue() as? Map<String, Any> ?: return null
        
        // Extract genre_ids if genres is missing
        val genres = json["genres"]
        val genreIds = json["genre_ids"] as? List<*>
        
        val updatedJson = if (genres == null && genreIds != null) {
            json.toMutableMap().apply {
                this["genres"] = genreIds.map { id ->
                    mapOf("id" to (id as Number).toInt(), "name" to "")
                }
            }
        } else {
            json
        }
        
        return delegate.fromJsonValue(updatedJson)
    }
}
