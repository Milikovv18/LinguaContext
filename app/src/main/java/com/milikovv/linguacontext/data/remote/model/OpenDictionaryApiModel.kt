package com.milikovv.linguacontext.data.remote.model


data class Phonetic(
    val text: String,
    val audio: String? = null
)

data class Definition(
    val definition: String,
    val example: String? = null,
    val synonyms: List<String>? = null,
    val antonyms: List<String>? = null
)

data class Meaning(
    val partOfSpeech: String,
    val definitions: List<Definition>
)

data class WordEntry(
    val word: String,
    val phonetic: String,
    val phonetics: List<Phonetic>,
    val origin: String,
    val meanings: List<Meaning>
)

typealias OpenDictionaryApiModel = List<WordEntry>
