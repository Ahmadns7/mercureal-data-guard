package com.mercureal.dataguard.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mercureal.dataguard.domain.Bundle
import com.mercureal.dataguard.domain.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val Context.store by preferencesDataStore("data_guard")

/** All persistence is a local DataStore file. Nothing leaves the device. */
class LocalStore(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    private val kSettings = stringPreferencesKey("settings")
    private val kBundles = stringPreferencesKey("bundles")

    val settings: Flow<Settings> = context.store.data.map { p ->
        p[kSettings]?.let { runCatching { json.decodeFromString<Settings>(it) }.getOrNull() } ?: Settings()
    }
    val bundles: Flow<List<Bundle>> = context.store.data.map { p ->
        p[kBundles]?.let { runCatching { json.decodeFromString(ListSerializer(Bundle.serializer()), it) }.getOrNull() }
            ?: emptyList()
    }

    suspend fun saveSettings(s: Settings) {
        context.store.edit { it[kSettings] = json.encodeToString(Settings.serializer(), s) }
    }
    suspend fun saveBundles(list: List<Bundle>) {
        context.store.edit { it[kBundles] = json.encodeToString(ListSerializer(Bundle.serializer()), list) }
    }
}
