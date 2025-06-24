package com.milikovv.linguacontext.data.remote

import com.milikovv.linguacontext.data.mapper.requestExplanation
import com.milikovv.linguacontext.data.mapper.requestFormality
import com.milikovv.linguacontext.data.mapper.toExplanationDetail
import com.milikovv.linguacontext.data.mapper.toFormalityDetail
import com.milikovv.linguacontext.data.mapper.toWordDetail
import com.milikovv.linguacontext.data.remote.model.OllamaGenerateRequest
import com.milikovv.linguacontext.data.remote.model.OllamaGenerateResponse
import com.milikovv.linguacontext.data.remote.model.OpenDictionaryApiModel
import com.milikovv.linguacontext.data.repo.IDetailDataItem
import com.milikovv.linguacontext.data.repo.SingleWordData
import com.milikovv.linguacontext.data.repo.WordDetail
import com.milikovv.linguacontext.domain.remote.RemoteDataSource
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

class RemoteRestfullDataSource @Inject constructor(
    val dictService: DictService,
    val ollamaService: OllamaService
) : RemoteDataSource {

    override suspend fun load(context: List<SingleWordData>): Flow<IDetailDataItem> {
        val selected = context.find { it.selected } ?: return flow {}
        return flow<IDetailDataItem> {
            // Simple operations with dictionary
            emit(
                try {
                    dictService.getEnWordData(selected.word).toWordDetail()
                } catch (_: Exception) {
                    WordDetail(selected.word, null)
                }
            )

            // Advanced context analysis
            val explanationPrompt = requestExplanation(selected.word, context.map { it.word })
            emit(ollamaService.generate(explanationPrompt).toExplanationDetail())

            val formalityPrompt = requestFormality(selected.word, context.map { it.word })
            emit(ollamaService.generate(formalityPrompt).toFormalityDetail())
        }.flowOn(Dispatchers.IO)
    }
}


interface DictService {
    @GET("en/{word}")
    suspend fun getEnWordData(@Path("word") word: String): OpenDictionaryApiModel

    companion object {
        const val BASE = "https://api.dictionaryapi.dev/api/v2/entries/"
    }
}

interface OllamaService {
    @POST("api/generate")
    suspend fun generate(@Body request: OllamaGenerateRequest): OllamaGenerateResponse
}
