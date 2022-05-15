package com.unopenedbox.molloo.network

import android.os.Build
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.fuel.coroutines.*
import com.github.kittinunf.fuel.serialization.kotlinxDeserializerOf
import com.github.kittinunf.result.Result
import com.unopenedbox.molloo.BuildConfig
import com.unopenedbox.molloo.struct.GenResponse
import com.unopenedbox.molloo.struct.resp.EmptyResp
import com.unopenedbox.molloo.struct.resp.SerialResp
import com.unopenedbox.molloo.struct.resp.StatusResp
import com.unopenedbox.molloo.struct.resp.TokenResp
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.coroutines.coroutineContext

class MollooRequest() {
    private var token: String = ""
    var serial: String = ""
    private val version = BuildConfig.VERSION_CODE

    companion object {
        const val BASE_URL = "http://192.168.3.95:3200"
    }

    init {
        FuelManager.instance.baseHeaders = mapOf(
            Headers.USER_AGENT to "MollooAndroid/$version",
            Headers.ACCEPT to "application/json"
        )
        FuelManager.instance.basePath = BASE_URL
    }


    suspend fun connect() {

    }

    /**
     * Fetch Server is aliving
     * @return true if server is aliving or false if not
     */
    suspend fun fetchAlive(): Boolean {
        return requestGet("/status", StatusResp.serializer()).fold(
            { result -> !result.isError && result.data?.alive ?: false },
            { error ->
                Log.e("MollooRequest", "fetchAlive: ${error.exception}")
                false
            }
        )
    }

    /**
     * Fetch new serial number for app
     */
    suspend fun fetchNewSerial(): String {
        return requestGet("/serial", SerialResp.serializer()).fold(
            {result -> if (result.isError) "" else result.data?.serial ?: "" },
            {error ->
                Log.e("MollooRequest", "fetchNewSerial: ${error.exception}")
                ""
            }
        )
    }

    suspend fun fetchToken(serial:String, username:String = "Default", isCamera:Boolean = false): String {
        return request(
            fuel = Fuel.upload("/token", Method.PUT, listOf(
                "serial" to serial,
                "isCamera" to isCamera.toString(),
                "username" to username,
            )),
            serializer = TokenResp.serializer(),
        ).fold(
            {result -> if (result.isError) "" else result.data?.token ?: ""},
            {error ->
                Log.e("MollooRequest", "fetchToken: ${error.exception}")
                ""
            }
        )
    }

    suspend fun requestECamPhoto(token:String):Boolean {
        return request(
            fuel = Fuel.upload("/photo/request", Method.POST, listOf(
                "token" to token,
            )),
            serializer = Unit.serializer(),
        ).fold(
            {result -> !result.isError},
            {error ->
                Log.e("MollooRequest", "requestECamPhoto: ${error.exception}")
                false
            }
        )
    }

    private suspend fun <T> requestGet(
        urlPath: String,
        serializer: KSerializer<T>,
        query: List<Pair<String, Any?>>? = null,
        timeout: Int = 5000
    ): Result<GenResponse<T>, FuelError> {
        return request(Fuel.get(urlPath, query), serializer, timeout)
    }

    private suspend fun <T> request(fuel:Request, serializer: KSerializer<T>, timeout: Int = 5000): Result<GenResponse<T>, FuelError> {
        return fuel.timeout(timeout)
            .awaitObjectResult(
                deserializable = kotlinxDeserializerOf(
                    loader = GenResponse.serializer(serializer),
                    json = Json {
                        ignoreUnknownKeys = true
                        coerceInputValues = true
                    }
                ),
                scope = Dispatchers.IO,
            )
    }
}