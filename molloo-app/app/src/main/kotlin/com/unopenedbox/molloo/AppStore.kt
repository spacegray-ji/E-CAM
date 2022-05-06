package com.unopenedbox.molloo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.settingStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class AppStore(private val context:Context) {

    private val serialKey = stringPreferencesKey("device_serial")
    private val firstUseKey = booleanPreferencesKey("first_use")

    val serial:Flow<String> = context.settingStore.data.map { preferences ->
        preferences[serialKey] ?: ""
    }

    val firstUse:Flow<Boolean> = context.settingStore.data.map { preferences ->
        preferences[firstUseKey] ?: true
    }

    suspend fun setSerial(serial: String) {
        context.settingStore.edit { pref ->
            pref[serialKey] = serial
        }
    }

    suspend fun setFirstUse(firstUse: Boolean) {
        context.settingStore.edit { pref ->
            pref[firstUseKey] = firstUse
        }
    }
}