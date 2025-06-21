package com.milikovv.linguacontext.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.milikovv.linguacontext.data.repo.DetailsState
import com.milikovv.linguacontext.data.repo.ServiceDataItem
import com.milikovv.linguacontext.data.repo.SingleWordData
import com.milikovv.linguacontext.data.repo.WordsContainerData
import com.milikovv.linguacontext.data.repo.WordsState
import com.milikovv.linguacontext.domain.repo.WordsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

@HiltViewModel
class WordsViewModel @Inject constructor(
    private val repo: WordsRepository
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
                    repo.loadDetailsData(data.words)
                        .collect { chunk ->
                            val currentItems = _detailsState.value.detailData
                            _detailsState.value = _detailsState.value.copy(
                                detailData = currentItems + chunk
                            )
                        }

                    // Finish loading
                    _detailsState.value = _detailsState.value.copy(
                        isLoading = false
                    )
                } catch (e: Exception) {
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
