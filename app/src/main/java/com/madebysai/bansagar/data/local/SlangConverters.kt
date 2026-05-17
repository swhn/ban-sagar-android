package com.madebysai.bansagar.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SlangConverters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStringList(value: String): List<String> =
        runCatching { json.decodeFromString<List<String>>(value) }.getOrDefault(emptyList())

    @TypeConverter
    fun toStringList(list: List<String>): String = json.encodeToString(list)

    @TypeConverter
    fun fromIntMap(value: String): Map<String, Int> =
        runCatching { json.decodeFromString<Map<String, Int>>(value) }.getOrDefault(emptyMap())

    @TypeConverter
    fun toIntMap(map: Map<String, Int>): String = json.encodeToString(map)
}
