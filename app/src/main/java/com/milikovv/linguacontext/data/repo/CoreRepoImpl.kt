package com.milikovv.linguacontext.data.repo

import com.milikovv.linguacontext.domain.remote.RemoteDataSource
import com.milikovv.linguacontext.domain.local.LocalDataSource
import com.milikovv.linguacontext.domain.repo.CoreRepository
import com.milikovv.linguacontext.domain.repo.ServiceRepository
import com.milikovv.linguacontext.domain.repo.WordsRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow

/**
 * Data repo implementation leveraging [LocalDataSource] and [RemoteDataSource] for both
 * OCR service and details downloaded from network.
 * @param localServiceDataSource local data source (service POV)
 * @param localWordsDataSource local data source (ViewModel POV)
 * @param remoteDataSource remote data source (networking)
 */
@Singleton
class CoreRepositoryImpl @Inject constructor(
    private val localServiceDataSource: LocalDataSource<ServiceDataItem>,
    private val localWordsDataSource: LocalDataSource<WordsContainerData>,
    private val remoteDataSource: RemoteDataSource
) : CoreRepository, ServiceRepository, WordsRepository {

    // ServiceRepository implementation
    override suspend fun saveServiceData(data: ServiceDataItem) = localServiceDataSource.set(data)

    // WordsRepository implementation
    override suspend fun getServiceData(): ServiceDataItem {
        return localServiceDataSource.get() ?: throw Exception("Service provided no data")
    }

    override suspend fun saveWordsData(data: WordsContainerData) = localWordsDataSource.set(data)
    override suspend fun getWordsData(): WordsContainerData {
        return localWordsDataSource.get() ?: throw Exception("No words data provided")
    }

    override suspend fun loadDetailsData(data: List<SingleWordData>): Flow<IDetailDataItem> = remoteDataSource.load(data)
}
