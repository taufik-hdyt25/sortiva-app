package com.sortiva.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sortiva_settings")

class DataStoreManager(private val context: Context) {

    companion object {
        val UNLOCKED_LEVEL = intPreferencesKey("unlocked_level")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
    }

    val unlockedLevelFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[UNLOCKED_LEVEL] ?: 1
        }

    val soundEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[SOUND_ENABLED] ?: true
        }
        
    val vibrationEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[VIBRATION_ENABLED] ?: true
        }

    suspend fun saveUnlockedLevel(level: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[UNLOCKED_LEVEL] ?: 1
            if (level > current) {
                preferences[UNLOCKED_LEVEL] = level
            }
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SOUND_ENABLED] = enabled
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[VIBRATION_ENABLED] = enabled
        }
    }
}
