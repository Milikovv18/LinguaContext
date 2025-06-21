package com.milikovv.linguacontext.domain.remote

import com.milikovv.linguacontext.data.repo.IDetailDataItem
import com.milikovv.linguacontext.data.repo.SingleWordData
import kotlinx.coroutines.flow.Flow

interface RemoteDataSource {
    suspend fun load(context: List<SingleWordData>) : Flow<IDetailDataItem>
}
