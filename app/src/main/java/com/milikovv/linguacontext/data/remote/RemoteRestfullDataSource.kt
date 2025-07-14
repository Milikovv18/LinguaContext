package com.milikovv.linguacontext.data.remote

import android.util.Log
import com.milikovv.linguacontext.data.mapper.emitExplanationDetailInto
import com.milikovv.linguacontext.data.mapper.requestExplanation
import com.milikovv.linguacontext.data.mapper.requestFormality
import com.milikovv.linguacontext.data.mapper.toFormalityDetail
import com.milikovv.linguacontext.data.mapper.toWordDetail
import com.milikovv.linguacontext.data.remote.model.OllamaGenerateRequest
import com.milikovv.linguacontext.data.remote.model.OllamaGenerateResponse
import com.milikovv.linguacontext.data.remote.model.OpenDictionaryApiModel
import com.milikovv.linguacontext.data.repo.IDetailDataItem
import com.milikovv.linguacontext.data.repo.SingleWordData
import com.milikovv.linguacontext.data.repo.WordDetail
import com.milikovv.linguacontext.domain.remote.RemoteDataSource
import com.milikovv.linguacontext.utils.Settings
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Streaming

/**
 * Remote (network) data source leveraging Retrofit for structured I/O.
 * @param dictService Open dictionary APIs
 * @param ollamaService Ollama APIs
 * @param modelNameFlow LLM model name changeable by user
 */
class RemoteRestfullDataSource @Inject constructor(
    val dictService: DictService,
    val ollamaService: OllamaService,
    private val modelNameFlow: Flow<Settings>
) : RemoteDataSource {
    // Use a volatile variable to hold the latest base URL
    @Volatile
    private var currentModelName: String? = null
    @Volatile
    private var thinkDisabled: Boolean = true

    // Collect the flow in a coroutine to update currentBaseUrl asynchronously
    fun monitorUpdates(scope: CoroutineScope) {
        scope.launch {
            modelNameFlow.collect {
                currentModelName = it.modelName
                thinkDisabled = it.thinkDisable
            }
        }
    }

    override suspend fun load(context: List<SingleWordData>): Flow<IDetailDataItem> {
        val modelName = currentModelName
        if (modelName == null)
            throw Exception("No model name provided")

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
            val explanationPrompt = requestExplanation(
                model = modelName, word = selected.word,
                context = context.map { it.word },
                doThink = !thinkDisabled
            )
            ollamaService.generateStream(explanationPrompt).emitExplanationDetailInto(this)

            val formalityPrompt = requestFormality(
                model = modelName, word = selected.word,
                context = context.map { it.word },
                doThink = !thinkDisabled
            )
            emit(ollamaService.generate(formalityPrompt).toFormalityDetail())
        }
            .flowOn(Dispatchers.IO)
            .catch { e -> Log.d("fefe", "Got error inside remote data source") }
    }
}

/**
 * Lists Open dictionary API entries with input parameters and expected response.
 */
interface DictService {
    @GET("en/{word}")
    suspend fun getEnWordData(@Path("word") word: String): OpenDictionaryApiModel

    companion object {
        const val BASE = "https://api.dictionaryapi.dev/api/v2/entries/"
    }
}

/**
 * Lists Ollama API entries with input parameters and expected response.
 */
interface OllamaService {
    @POST("api/generate")
    suspend fun generate(@Body request: OllamaGenerateRequest): OllamaGenerateResponse

    @Streaming
    @POST("api/generate")
    suspend fun generateStream(@Body request: OllamaGenerateRequest): ResponseBody
}
