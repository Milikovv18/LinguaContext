package com.milikovv.linguacontext.di

import com.milikovv.linguacontext.data.remote.DictService
import com.milikovv.linguacontext.data.remote.OllamaService
import com.milikovv.linguacontext.data.remote.RemoteRestfullDataSource
import com.milikovv.linguacontext.utils.DataStoreManager
import com.milikovv.linguacontext.utils.OkHttpBaseUrlInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UserBaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DebugBaseUrl


@Module
@InstallIn(SingletonComponent::class)
object UserUrlModule {
    @UserBaseUrl
    @Provides
    @Singleton
    fun provideUserBaseUrl(dataStoreManager: DataStoreManager): Flow<String> {
        return dataStoreManager.baseUrlFlow
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DebugUrlModule {
    @DebugBaseUrl
    @Provides
    @Singleton
    fun provideDebugBaseUrl(): Flow<String> = flow { emit("http://localhost:11434") }
}


@Module
@InstallIn(SingletonComponent::class)
object RemoteRestfulDataSourceModule {
    @Provides
    @Singleton
    fun provideDataSource(@DebugBaseUrl ollamaBase: Flow<String>): RemoteRestfullDataSource {
        val baseUrlInterceptor = OkHttpBaseUrlInterceptor(ollamaBase)
        baseUrlInterceptor.startCollecting(CoroutineScope(SupervisorJob() + Dispatchers.Default))

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

        return RemoteRestfullDataSource(dictService, ollamaService)
    }
}
