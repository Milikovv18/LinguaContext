package com.milikovv.linguacontext.data.mapper

import com.milikovv.linguacontext.data.remote.model.OpenDictionaryApiModel
import com.milikovv.linguacontext.data.repo.IDetailDataItem
import com.milikovv.linguacontext.data.repo.WordDetail


/**
 * Custom mapper of [OpenDictionaryApiModel] returned by Open dictionary API to a processable data
 * class [WordDetail].
 */
fun OpenDictionaryApiModel.toWordDetail(): IDetailDataItem {
    // Careful with cases where one word might have multiple genders based on definition etc.
    val meaning = this.first()
    return WordDetail(meaning.word, meaning.phonetic)
}
