package com.milikovv.linguacontext.utils

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Substitutes cumbersome [AccessibilityNodeInfo.getBoundsInScreen] syntax usage.
 */
internal val AccessibilityNodeInfo.bounds : Rect get() {
    val bounds = Rect()
    this.getBoundsInScreen(bounds)
    return bounds
}
