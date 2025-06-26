package com.milikovv.linguacontext.utils

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody


class OkHttpBaseUrlInterceptor(
    private val baseUrlFlow: Flow<String>
) : Interceptor {

    // Use a volatile variable to hold the latest base URL
    @Volatile
    private var currentBaseUrl: HttpUrl? = null

    // Collect the flow in a coroutine to update currentBaseUrl asynchronously
    fun monitorUpdates(scope: CoroutineScope) {
        scope.launch {
            baseUrlFlow.collect { urlString ->
                try {
                    currentBaseUrl = HttpUrl.parse(urlString)
                } catch (_: Exception) {
                    // TODO Invalid URL, ignore or log
                }
            }
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val baseUrl = currentBaseUrl

        // If no base URL set yet, proceed with original request
        if (baseUrl == null) {
            return chain.proceed(originalRequest)
        }

        val originalHttpUrl = originalRequest.url()

        // Rebuild the URL using scheme, host, and port from currentBaseUrl
        val newUrl = originalHttpUrl.newBuilder()
            .scheme(baseUrl.scheme())
            .host(baseUrl.host())
            .port(baseUrl.port())
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        val response = chain.proceed(newRequest)
        val rawJson = response.body()!!.string()
        Log.d("RawJsonResponse", rawJson)

        // Re-create response before returning because body can be read only once
        return response.newBuilder()
            .body(ResponseBody.create(response.body()!!.contentType(), rawJson))
            .build()
    }
}
