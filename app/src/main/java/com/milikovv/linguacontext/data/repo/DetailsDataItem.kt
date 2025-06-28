package com.milikovv.linguacontext.data.repo

/**
 * Abstract info about selected word for easier mappings.
 */
sealed interface IDetailDataItem

/**
 * Details of selected word: phonetic
 */
data class WordDetail(
    val word: String,
    val phonetic: String?
) : IDetailDataItem

/**
 * Details of selected word's meaning.
 */
data class ExplanationDetail(
    val text: String
) : IDetailDataItem

/**
 * Details of selected word's formality in a form of float in between 0 and 1.
 */
data class FormalityDetail(
    val formality: Float
) : IDetailDataItem
