package com.milikovv.linguacontext.ui

import android.graphics.PointF
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.milikovv.linguacontext.data.repo.SingleWordData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

@Composable
fun RectanglesOverlay(
    scale: PointF,
    words: List<SingleWordData>,
    selectedWord: SingleWordData?,
    onRectClick: (SingleWordData) -> Unit
) {
    val sortedWords = remember(words) { words.sortedBy { it.bbox.top } }
    val alphas = remember { sortedWords.map { Animatable(0f) } }

    LaunchedEffect(sortedWords) {
        alphas.forEachIndexed { index, animatable ->
            launch {
                delay(index * 10L)
                animatable.animateTo(1f, tween(500))
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CircledRectanglesCanvas(scale, words = sortedWords, alphas = alphas, selectedWord = selectedWord)
        ClickableRectanglesDetector(scale, words = sortedWords, onRectClick = onRectClick)
    }
}

@Composable
fun CircledRectanglesCanvas(
    scale: PointF,
    words: List<SingleWordData>,
    alphas: List<Animatable<Float, AnimationVector1D>>,
    selectedWord: SingleWordData?
) {
    // Infinite transition for pulsing animation
    val infiniteTransition = rememberInfiniteTransition()

    // Animate radius scale between 0.9f and 1.1f repeatedly
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val scaleX = size.width / scale.x
        val scaleY = size.height / scale.y

        // Calculate pulsing circle path if a word is selected
        val circlePath = selectedWord?.let { word ->
            val rect = word.bbox
            val centerX = (rect.left + rect.right) / 2f * scaleX
            val centerY = (rect.top + rect.bottom) / 2f * scaleY
            val baseRadius = max(
                (rect.right - rect.left) * scaleX,
                (rect.bottom - rect.top) * scaleY
            ) / 2f + 16.dp.toPx() // base radius with padding

            val pulsingRadius = baseRadius * pulseScale

            val brect = androidx.compose.ui.geometry.Rect(
                left = centerX - pulsingRadius,
                top = centerY - baseRadius / 2,
                right = centerX + pulsingRadius,
                bottom = centerY + baseRadius / 2
            )
            val cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()) // adjust as needed

            Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = brect,
                        topLeft = cornerRadius,
                        topRight = cornerRadius,
                        bottomLeft = cornerRadius,
                        bottomRight = cornerRadius
                    )
                )
            }
        }

        words.forEachIndexed { index, word ->
            val rect = word.bbox
            val alpha = alphas.getOrNull(index)?.value ?: 0f

            val left = rect.left * scaleX
            val top = rect.top * scaleY
            val right = rect.right * scaleX
            val bottom = rect.bottom * scaleY

            val drawColor = Color.Blue.copy(alpha = 0.3f * alpha)
            val strokeColor = Color.Blue.copy(alpha = alpha)

            if (circlePath != null) {
                clipPath(circlePath, clipOp = ClipOp.Intersect) {
                    drawRoundRect(
                        color = drawColor,
                        topLeft = Offset(left, top),
                        size = Size(right - left, bottom - top),
                        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                        style = Fill
                    )
                    drawRoundRect(
                        color = strokeColor,
                        topLeft = Offset(left, top),
                        size = Size(right - left, bottom - top),
                        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            } else {
                drawRoundRect(
                    color = drawColor,
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top),
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                    style = Fill
                )
                drawRoundRect(
                    color = strokeColor,
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top),
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun ClickableRectanglesDetector(
    scale: PointF,
    words: List<SingleWordData>,
    onRectClick: (SingleWordData) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(words) {
                detectTapGestures { tapOffset ->
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val scaleX = canvasWidth / scale.x
                    val scaleY = canvasHeight / scale.y

                    val clickedWord = words.firstOrNull { word ->
                        val rect = word.bbox
                        val left = rect.left * scaleX
                        val top = rect.top * scaleY
                        val right = rect.right * scaleX
                        val bottom = rect.bottom * scaleY

                        tapOffset.x in left..right && tapOffset.y in top..bottom
                    }
                    clickedWord?.let(onRectClick)
                }
            }
    )
}


