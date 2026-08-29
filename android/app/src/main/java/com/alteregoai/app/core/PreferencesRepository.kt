package com.alteregoai.app.core

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appPreferences by preferencesDataStore(name = "alter_ego_preferences")

class PreferencesRepository(private val context: Context) {
    private val themeKey = stringPreferencesKey("theme")
    val theme: Flow<String> = context.appPreferences.data.map { it[themeKey] ?: "Cinematic" }
    suspend fun setTheme(value: String) { context.appPreferences.edit { it[themeKey] = value } }
}
