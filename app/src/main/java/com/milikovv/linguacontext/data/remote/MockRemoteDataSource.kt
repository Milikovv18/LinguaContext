package com.milikovv.linguacontext.data.remote

import com.milikovv.linguacontext.data.repo.ExplanationDetail
import com.milikovv.linguacontext.data.repo.FormalityDetail
import com.milikovv.linguacontext.data.repo.IDetailDataItem
import com.milikovv.linguacontext.data.repo.SingleWordData
import com.milikovv.linguacontext.data.repo.WordDetail
import com.milikovv.linguacontext.domain.remote.RemoteDataSource
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.seconds

/**
 * Mock data source for quick manual tests.
 * Emits mock data in a form of [IDetailDataItem] every second.
 */
class MockRemoteDataSource @Inject constructor() : RemoteDataSource {
    override suspend fun load(context: List<SingleWordData>) : Flow<IDetailDataItem> {
        val selectedWord = context.find { it.selected } ?: return flow {}
        return flow {
            emit(WordDetail(selectedWord.word, selectedWord.word))
            kotlinx.coroutines.delay(1.seconds)
            emit(ExplanationDetail("A large text with context word translation and explanation"))
            kotlinx.coroutines.delay(1.seconds)
            emit(FormalityDetail(0.7f))
        }
    }
}
