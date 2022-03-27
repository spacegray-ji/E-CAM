package com.molloo.resp

import com.molloo.structure.PhotoInfo
import com.molloo.structure.TokenInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.gildor.coroutines.okhttp.await
import java.util.concurrent.TimeUnit

class MainRequest {
    companion object {
        const val serverURL = "http://192.168.3.95:3200"
    }

    private val client = OkHttpClient.Builder().apply {
        connectTimeout(5, TimeUnit.SECONDS)
        readTimeout(10, TimeUnit.SECONDS)
    }.build()
    private val JsonParser = Json {
        ignoreUnknownKeys = true
    }

    var token = ""
    val isInited get() = this.token.isNotEmpty()

    suspend fun initToken(serial: String, username: String = "Default"):String {
        if (!isInited) {
            val tokenRes = this.getToken(serial, username)
            if (tokenRes != null) {
                token = tokenRes
            }
        }
        return token
    }

    suspend fun getPhotoList():Array<PhotoInfo> {
        try {
            val photoResponse = requestGet("/photo", PhotoResBody.serializer()) {
                addQueryParameter("token", token)
            }
            return if (photoResponse.isError) {
                arrayOf()
            } else {
                photoResponse.data.photos
            }
        } catch (err: Exception) {
            err.printStackTrace()
        }
        return arrayOf()
    }

    suspend fun takePhoto():PhotoInfo? {
        try {
            val photoResponse = requestGet("/photo/take", PhotoResBody.serializer()) {
                addQueryParameter("token", token)
            }
            return if (photoResponse.isError) {
                null
            } else {
                photoResponse.data.photos.getOrNull(0)
            }
        } catch (err: Exception) {
            err.printStackTrace()
        }
        return null
    }

    private suspend fun <T> requestGet(urlPath:String, contentSerializer: KSerializer<T>, fn:HttpUrl.Builder.() -> Any): GenResponse<T> {
        val httpBuilder = HttpUrl.parse("$serverURL$urlPath")!!.newBuilder().apply {
            fn(this)
        }
        val request = Request.Builder().apply {
            url(httpBuilder.build())
            get()
        }
        return request(contentSerializer = contentSerializer, req = request)
    }

    private suspend fun getToken(serial: String, username: String = "Default"):String? {
        try {
            val tokenResponse = requestPut("/token", TokenInfo.serializer()) {
                addFormDataPart("serial", serial)
                addFormDataPart("username", username)
                addFormDataPart("isCamera", false.toString())
            }
            return if (tokenResponse.isError) {
                null
            } else {
                tokenResponse.data.token
            }
        } catch (err: Exception) {
            err.printStackTrace()
        }
        return null
    }

    private suspend fun <T> requestPut(urlPath:String, contentSerializer: KSerializer<T>, fn:MultipartBody.Builder.() -> Any): GenResponse<T> {
        val request = Request.Builder().apply {
            url("$serverURL$urlPath")
            put(MultipartBody.Builder().apply {
                setType(MultipartBody.FORM)
                fn(this)
            }.build())
        }
        return request(contentSerializer = contentSerializer, req = request)
    }

    private suspend fun <T> request(req:Request.Builder, contentSerializer: KSerializer<T>): GenResponse<T> {
        var resp: GenResponse<T>
        withContext(Dispatchers.IO) {
            val result = client.newCall(req.build()).await()
            val resultBody = result.body()?.string() ?: throw NullPointerException("result.body should be not null!")
            resp = JsonParser.decodeFromString(GenResponse.serializer(contentSerializer), resultBody)
            println("JSON: $resultBody")
        }
        return resp
    }
}