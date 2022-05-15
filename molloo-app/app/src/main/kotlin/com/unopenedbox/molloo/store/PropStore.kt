package com.unopenedbox.molloo.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.prefStore: DataStore<Preferences> by preferencesDataStore(name = "app_pref")

class PropStore(private val propStore:DataStore<Preferences>) {

    private val deviceSerialKey = stringPreferencesKey("device_serial")
    private val camSerialKey = stringPreferencesKey("cam_serial")
    private val firstUseKey = booleanPreferencesKey("first_use")
    private val usernameKey = stringPreferencesKey("username")
    private val usernameListKey = stringPreferencesKey("usernameList")

    val deviceSerial: Flow<String> = propStore.data.map { preferences ->
        preferences[deviceSerialKey] ?: ""
    }

    val camSerial: Flow<String> = propStore.data.map { preferences ->
        preferences[camSerialKey] ?: ""
    }

    val firstUse: Flow<Boolean> = propStore.data.map { preferences ->
        preferences[firstUseKey] ?: true
    }

    val username: Flow<String> = propStore.data.map { preferences ->
        preferences[usernameKey] ?: "Default"
    }

    val usernameList: Flow<List<String>> = propStore.data.map { preferences ->
        preferences[usernameListKey]?.split(",") ?: listOf("Default")
    }

    suspend fun setDeviceSerial(serial: String) {
        propStore.edit { pref ->
            pref[deviceSerialKey] = serial
        }
    }

    suspend fun setCamSerial(serial: String) {
        propStore.edit { pref ->
            pref[camSerialKey] = serial
        }
    }

    suspend fun setFirstUse(firstUse: Boolean) {
        propStore.edit { pref ->
            pref[firstUseKey] = firstUse
        }
    }

    suspend fun setUsername(username: String) {
        propStore.edit { pref ->
            pref[usernameKey] = username
        }
    }

    suspend fun setUsernameList(usernameList: List<String>) {
        propStore.edit { pref ->
            pref[usernameListKey] = usernameList.joinToString(",")
        }
    }
}