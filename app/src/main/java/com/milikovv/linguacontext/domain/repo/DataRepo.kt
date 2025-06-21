package com.milikovv.linguacontext.domain.repo

import com.milikovv.linguacontext.data.repo.IDetailDataItem
import com.milikovv.linguacontext.data.repo.ServiceDataItem
import com.milikovv.linguacontext.data.repo.SingleWordData
import com.milikovv.linguacontext.data.repo.WordsContainerData
import kotlinx.coroutines.flow.Flow

// Full repository interface (no read/write restrictions)
interface CoreRepository {
    suspend fun saveServiceData(data: ServiceDataItem)
    suspend fun getServiceData(): ServiceDataItem

    suspend fun saveWordsData(data: WordsContainerData)
    suspend fun getWordsData(): WordsContainerData

    suspend fun loadDetailsData(data: List<SingleWordData>): Flow<IDetailDataItem>
}


// Repository interface exposed to AccessibilityService (write-only)
interface ServiceRepository {
    // Only writing scanned data to service
    suspend fun saveServiceData(data: ServiceDataItem)
}

// Repository interface exposed to Activity (read/write)
interface WordsRepository {
    // Read access to scanned data
    suspend fun getServiceData(): ServiceDataItem

    suspend fun saveWordsData(data: WordsContainerData)
    suspend fun getWordsData(): WordsContainerData

    suspend fun loadDetailsData(data: List<SingleWordData>): Flow<IDetailDataItem>
}
