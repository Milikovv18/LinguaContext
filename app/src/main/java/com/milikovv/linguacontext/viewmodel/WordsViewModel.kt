package com.milikovv.linguacontext.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.milikovv.linguacontext.data.repo.DetailsState
import com.milikovv.linguacontext.data.repo.ExplanationDetail
import com.milikovv.linguacontext.data.repo.ServiceDataItem
import com.milikovv.linguacontext.data.repo.SingleWordData
import com.milikovv.linguacontext.data.repo.WordsContainerData
import com.milikovv.linguacontext.data.repo.WordsState
import com.milikovv.linguacontext.domain.repo.WordsRepository
import com.milikovv.linguacontext.utils.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

/**
 * Observes [WordsState] and [DetailsState], implements initial screenshot scanning and processes
 * remote data. Handles user's word selection.
 */
@HiltViewModel
class WordsViewModel @Inject constructor(
    private val repo: WordsRepository,
    private val settingsStore: DataStoreManager
) : ViewModel() {
    private val _serviceState = MutableStateFlow(WordsState())
    val serviceState: StateFlow<WordsState> = _serviceState.asStateFlow()

    private val _detailsState = MutableStateFlow(DetailsState())
    val detailsState: StateFlow<DetailsState> = _detailsState.asStateFlow()

    private var analysisJob: Job? = null

    init {
        viewModelScope.launch {
            val processedData = scanImage(repo.getServiceData())
            _serviceState.value = WordsState(data = processedData)
        }
    }

    fun selectWord(selectedWord: SingleWordData?) {
        val currentState = _serviceState.value
        currentState.data?.let { data ->
            val currentWords = data.words

            val updatedWords = currentWords.map { word ->
                word.copy(selected = (selectedWord != null && word.id == selectedWord.id))
            }

            val updatedData = data.copy(words = updatedWords)
            _serviceState.value = currentState.copy(data = updatedData)
        }
    }

    fun requestAnalysis() {
        if (analysisJob != null) return // prevent multiple starts

        _serviceState.value.data?.let { data ->
            analysisJob = viewModelScope.launch {
                _detailsState.value = DetailsState(isLoading = true)
                try {
                    // Loading chunks of data
                    var thinkingText = ""
                    var answerText = ""
                    val chunksFlow = repo.loadDetailsData(data.words)
                    chunksFlow.collect { chunk ->
                        when (chunk) {
                            is ExplanationDetail -> {
                                // Append the new token to the accumulated text
                                thinkingText += chunk.thinkingText
                                answerText += chunk.answerText
                                // Update the state with the same item, but new text
                                val currentItems = _detailsState.value.detailData
                                val updatedItems =
                                    if (currentItems.lastOrNull() is ExplanationDetail) {
                                        currentItems.dropLast(1) + chunk.copy(
                                            thinkingText = thinkingText,
                                            answerText = answerText
                                        )
                                    } else {
                                        currentItems + chunk.copy(
                                            thinkingText = thinkingText,
                                            answerText = answerText
                                        )
                                    }
                                _detailsState.value =
                                    _detailsState.value.copy(detailData = updatedItems)
                            }

                            else -> {
                                val currentItems = _detailsState.value.detailData
                                _detailsState.value = _detailsState.value.copy(
                                    detailData = currentItems + chunk
                                )
                            }
                        }
                    }

                    // Finish loading
                    _detailsState.value = _detailsState.value.copy(
                        isLoading = false
                    )
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    e.printStackTrace()
                    _detailsState.value = _detailsState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun stopAnalysis() {
        analysisJob?.cancel()
        analysisJob = null
        settingsStore.overrideSettings(skipThinking = false)
    }

    fun skipThinking() {
        stopAnalysis()
        settingsStore.overrideSettings(skipThinking = true)
        requestAnalysis()
    }


    private suspend fun scanImage(rawData: ServiceDataItem): WordsContainerData {
        val image = InputImage.fromBitmap(rawData.image, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        return WordsContainerData(
            rawData.image,
            try {
                val visionText = recognizer.process(image).await()
                visionText.textBlocks
                    .flatMap { it.lines }
                    .flatMap { it.elements }
                    .mapIndexedNotNull { id, element ->
                        element.boundingBox?.let { SingleWordData(id, element.text, it) }
                    }
            } catch (_: Exception) {
                emptyList()
            }
        )
    }
}
