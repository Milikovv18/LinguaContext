package com.milikovv.linguacontext.di

import com.milikovv.linguacontext.data.local.MemoryLocalDataSource
import com.milikovv.linguacontext.data.remote.RemoteRestfullDataSource
import com.milikovv.linguacontext.data.repo.CoreRepositoryImpl
import com.milikovv.linguacontext.data.repo.ServiceDataItem
import com.milikovv.linguacontext.data.repo.WordsContainerData
import com.milikovv.linguacontext.domain.local.LocalDataSource
import com.milikovv.linguacontext.domain.remote.RemoteDataSource
import com.milikovv.linguacontext.domain.repo.ServiceRepository
import com.milikovv.linguacontext.domain.repo.WordsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
interface LocalRepoModule {
    @Binds
    @Singleton
    fun bindInMemServiceRepo(repo: MemoryLocalDataSource<ServiceDataItem>) : LocalDataSource<ServiceDataItem>

    @Binds
    @Singleton
    fun bindInMemWordRepo(repo: MemoryLocalDataSource<WordsContainerData>) : LocalDataSource<WordsContainerData>
}

@Module
@InstallIn(SingletonComponent::class)
interface RemoteRepoModule {
    @Binds
    @Singleton
    fun provideEmptyRepo(repo: RemoteRestfullDataSource): RemoteDataSource
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideCoreRepository(
        localServiceDataSource: LocalDataSource<ServiceDataItem>,
        localWordsDataSource: LocalDataSource<WordsContainerData>,
        remoteDataSource: RemoteDataSource
    ): CoreRepositoryImpl = CoreRepositoryImpl(localServiceDataSource, localWordsDataSource, remoteDataSource)

    @Provides
    fun provideServiceRepository(coreRepository: CoreRepositoryImpl): ServiceRepository = coreRepository

    @Provides
    fun provideActivityRepository(coreRepository: CoreRepositoryImpl): WordsRepository = coreRepository
}

