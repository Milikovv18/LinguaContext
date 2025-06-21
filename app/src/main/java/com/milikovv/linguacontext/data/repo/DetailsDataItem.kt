package com.milikovv.linguacontext.data.repo

sealed interface IDetailDataItem

@JvmInline
value class WordDetail(val word: String) : IDetailDataItem

@JvmInline
value class ExplanationDetail(val text: String) : IDetailDataItem

@JvmInline
value class FormalityDetail(val value: Float) : IDetailDataItem
