package com.milikovv.linguacontext.di

import com.milikovv.linguacontext.data.remote.DictService
import com.milikovv.linguacontext.data.remote.OllamaService
import com.milikovv.linguacontext.data.remote.RemoteRestfullDataSource
import com.milikovv.linguacontext.utils.OkHttpBaseUrlInterceptor
import com.milikovv.linguacontext.utils.Settings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Initializes Open dictionary and Ollama APIs. Sets up [OkHttpBaseUrlInterceptor] to monitor
 * base URL changes.
 */
@Module
@InstallIn(SingletonComponent::class)
object RemoteRestfulDataSourceModule {
    @Provides
    @Singleton
    fun provideDataSource(ollamaBase: Flow<Settings>): RemoteRestfullDataSource {
        val baseUrlInterceptor = OkHttpBaseUrlInterceptor(ollamaBase.map{ it.baseUrl }.filterNotNull())
        baseUrlInterceptor.monitorUpdates(CoroutineScope(SupervisorJob() + Dispatchers.Default))

        val dictService: DictService = Retrofit.Builder()
            .baseUrl(DictService.BASE)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DictService::class.java)

        val ollamaService: OllamaService = Retrofit.Builder()
            .client(OkHttpClient.Builder()
                .addInterceptor(baseUrlInterceptor)
                .connectTimeout(0, TimeUnit.MILLISECONDS)
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .writeTimeout(0, TimeUnit.MILLISECONDS)
                .callTimeout(0, TimeUnit.MILLISECONDS)
                .build())
            .baseUrl("http://dummy")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OllamaService::class.java)

        val dataSource = RemoteRestfullDataSource(dictService, ollamaService, ollamaBase)
        dataSource.monitorUpdates(CoroutineScope(SupervisorJob() + Dispatchers.Default))
        return dataSource
    }
}
