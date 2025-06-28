package com.milikovv.linguacontext.data.repo

import android.graphics.Bitmap
import android.graphics.Rect

/**
 * Info about single word displayed on screen: the word itself, bounding box, and is it selected by user.
 */
data class SingleWordData(
    val id: Int,
    val word: String,
    val bbox: Rect,
    val selected: Boolean = false
)

/**
 * A full list of [SingleWordData] currently visible on screen together with a screenshot of these words.
 */
data class WordsContainerData(
    val background: Bitmap,
    val words: List<SingleWordData>
)
