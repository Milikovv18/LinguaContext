package com.milikovv.linguacontext.data.repo

import android.graphics.Bitmap
import android.graphics.Rect

data class SingleWordData(
    val id: Int,
    val word: String,
    val bbox: Rect,
    val selected: Boolean = false
)

data class WordsContainerData(
    val background: Bitmap,
    val words: List<SingleWordData>
)
