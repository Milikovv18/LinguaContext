package com.milikovv.linguacontext.data.repo


data class WordsState(
    val data: WordsContainerData? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class DetailsState(
    val detailData: List<IDetailDataItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAd: Boolean = false
)
