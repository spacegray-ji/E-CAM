package com.unopenedbox.molloo.ui.model

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unopenedbox.molloo.network.MollooRequest
import com.unopenedbox.molloo.store.PropStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Molloo Server - Client ViewModel
 */
class MollooClientViewModel : ViewModel() {
    val request = MollooRequest()
    // States
    // server is active
    private val _serverActive = MutableStateFlow(false)
    val serverActive = _serverActive.asStateFlow()
    // is activity prepared to start
    private val _isInited = MutableStateFlow(false)
    val isInited = _isInited.asStateFlow()
    // device serial
    private val _deviceSerial = MutableStateFlow("")
    val deviceSerial = _deviceSerial.asStateFlow()
    // cam serial
    private val _camSerial = MutableStateFlow("")
    val camSerial = _camSerial.asStateFlow()
    fun setCamSerial(camSerial: String) {
        _camSerial.value = camSerial
    }
    // device token
    private val _deviceToken = MutableStateFlow("")
    val deviceToken = _deviceToken.asStateFlow()
    // cam token
    private val _camToken = MutableStateFlow("")
    val camToken = _camToken.asStateFlow()
    // Username
    private val _username = MutableStateFlow("Default")
    val username = _username.asStateFlow()
    fun setUsername(username: String) {
        _username.value = username
    }


    init {
        viewModelScope.launch {
            _serverActive.value = request.fetchAlive()
            _isInited.value = true
            launch {
                username.collect { uname ->
                    Log.d("MollooClientViewModel", "Changed username: $uname, CamSerial: ${camSerial.value}, DeviceSerial: ${deviceSerial.value}")
                    val camSerialValue = camSerial.value
                    if (camSerialValue.isNotEmpty()) {
                        _camToken.value = request.fetchToken(camSerialValue, uname)
                        Log.d("MollooClientViewModel", "Cam Token: ${camToken.value}")
                    }
                    val deviceSerialValue = deviceSerial.value
                    if (deviceSerialValue.isNotEmpty()) {
                        _deviceToken.value = request.fetchToken(deviceSerialValue, uname)
                    }
                }
            }
            launch {
                deviceSerial.collect { serial ->
                    if (serial.isNotEmpty()) {
                        _deviceToken.value = request.fetchToken(serial, username.value)
                    }
                }
            }
            launch {
                camSerial.collect { serial ->
                    if (serial.isNotEmpty()) {
                        _camToken.value = request.fetchToken(serial, username.value)
                    }
                }
            }
        }
    }

    suspend fun setupSerial(deviceSerial: String, camSerial: String) {
        val deviceAllocSerial = deviceSerial.ifEmpty { request.fetchNewSerial() }
        if (_deviceSerial.value != deviceAllocSerial) {
            _deviceSerial.value = deviceAllocSerial
        }
        updateCamSerial(camSerial)
    }

    suspend fun updateCamSerial(serial: String) {
        if (camSerial.value != serial) {
            _camSerial.value = serial
        }
    }
}