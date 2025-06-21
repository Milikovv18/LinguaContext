package com.milikovv.linguacontext.utils

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

// Simplify node's bounds in screen access
internal val AccessibilityNodeInfo.bounds : Rect get() {
    val bounds = Rect()
    this.getBoundsInScreen(bounds)
    return bounds
}
