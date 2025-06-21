package com.milikovv.linguacontext.data.repo

import android.graphics.Bitmap
import android.graphics.Rect


// Data class to hold node info: text and bounds
data class NodeInfoData(
    val text: String,
    val bounds: Rect
)

data class ServiceDataItem(
    var text: List<NodeInfoData>,
    val image: Bitmap
)
