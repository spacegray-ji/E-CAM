package com.unopenedbox.molloo.ui.model

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.unopenedbox.molloo.network.MollooRequest
import com.unopenedbox.molloo.store.PropStore
import com.unopenedbox.molloo.struct.DentalHistory
import com.unopenedbox.molloo.struct.PhotoInfo
import com.unopenedbox.molloo.struct.photo.PhotoPagingSource
import com.unopenedbox.molloo.struct.photo.PhotoPagingSource2
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
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
    private val _username = MutableStateFlow("")
    val username = _username.asStateFlow()
    fun setUsername(username: String) {
        _username.value = username
    }
    // PhotoFlow
    var photoPagingSource2: PhotoPagingSource2? = null
    @OptIn(ExperimentalCoroutinesApi::class)
    val photoItemFlow = camToken.flatMapLatest { token ->
        Log.d("PhotoItemFlow", "Token: $token")
        Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = true, initialLoadSize = 10),
            pagingSourceFactory = {
                PhotoPagingSource2(
                    request,
                    token,
                ).also {
                    photoPagingSource2 = it
                }
            }
        ).flow.cachedIn(viewModelScope)
    }
    // Dental List
    private val _dentalList = MutableStateFlow(emptyList<DentalHistory>())
    val dentalList = _dentalList.asStateFlow()
    fun setDentalList(dentalList: List<DentalHistory>) {
        _dentalList.value = dentalList
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
                        updateCamToken(camSerialValue, uname)
                        Log.d("MollooClientViewModel", "Cam Token: ${camToken.value}")
                    }
                    val deviceSerialValue = deviceSerial.value
                    if (deviceSerialValue.isNotEmpty()) {
                        updateDeviceToken(deviceSerialValue, uname)
                    }
                }
            }
            launch {
                deviceSerial.collect { serial ->
                    if (serial.isNotEmpty()) {
                        updateDeviceToken(serial, username.value)
                    }
                }
            }
            launch {
                camSerial.collect { serial ->
                    if (serial.isNotEmpty()) {
                        // updateCamToken(serial, username.value)
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

    private suspend fun updateCamToken(serial:String, username: String) {
        Log.d("MollooClientViewModel", "updateCamToken: $serial / $username (now: ${camToken.value})")
        if (camToken.value.isNotEmpty()) {
            val tokenResp = request.verifyToken(camToken.value)
            if (tokenResp != null) {
                if (tokenResp.valid && tokenResp.username == username) {
                    // Skip
                    return
                }
            }
        }
        _camToken.value = request.fetchToken(serial, username)
    }

    private suspend fun updateDeviceToken(serial:String, username: String) {
        if (deviceToken.value.isNotEmpty()) {
            val tokenResp = request.verifyToken(deviceToken.value)
            if (tokenResp != null) {
                if (tokenResp.valid && tokenResp.username == username) {
                    // Skip
                    return
                }
            }
        }
        _deviceToken.value = request.fetchToken(serial, username)
    }

    fun updateCamSerial(serial: String) {
        if (camSerial.value != serial) {
            _camSerial.value = serial
        }
    }
}