package com.milikovv.linguacontext.data.repo

/**
 * [WordsContainerData] converted to ViewModel UI state.
 */
data class WordsState(
    val data: WordsContainerData? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Implementations of [IDetailDataItem] converted to ViewModel UI state.
 */
data class DetailsState(
    val detailData: List<IDetailDataItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAd: Boolean = false
)
