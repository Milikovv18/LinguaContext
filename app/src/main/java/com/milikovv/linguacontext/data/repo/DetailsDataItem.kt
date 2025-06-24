package com.milikovv.linguacontext.data.repo

sealed interface IDetailDataItem

data class WordDetail(
    val word: String,
    val phonetic: String?
) : IDetailDataItem


data class ExplanationDetail(
    val text: String
) : IDetailDataItem


data class FormalityDetail(
    val formality: Float
) : IDetailDataItem
