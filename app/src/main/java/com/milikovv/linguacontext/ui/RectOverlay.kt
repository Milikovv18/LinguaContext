package com.milikovv.linguacontext.ui

import android.graphics.PointF
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateRectAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.milikovv.linguacontext.data.repo.SingleWordData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun RectanglesOverlay(
    modifier: Modifier = Modifier,
    scale: PointF,
    words: List<SingleWordData>,
    selectedWord: SingleWordData?,
    isLoading: Boolean,
    onRectClick: (SingleWordData) -> Unit
) {
    val sortedWords = remember(words) { words.sortedBy { it.bbox.top } }
    val alphas = remember { sortedWords.map { Animatable(0f) } }

    val totalDuration = 1000L
    val singleAnimationDuration = 500L
    val count = sortedWords.size
    val perElementDelay = if (count > 1) {
        (totalDuration - singleAnimationDuration) / (count - 1)
    } else 0L

    LaunchedEffect(sortedWords) {
        alphas.forEachIndexed { index, animatable ->
            launch {
                delay(index * perElementDelay)
                animatable.animateTo(
                    1f,
                    tween(singleAnimationDuration.toInt())
                )
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedRectRegion(scale = scale, words = sortedWords, alphas = alphas,
            selectedWord = selectedWord, isLoading = isLoading)
        ClickableRectanglesDetector(scale, words = sortedWords, onRectClick = onRectClick)
    }
}


@Composable
fun AnimatedRectRegion(
    scale: PointF,
    words: List<SingleWordData>,
    alphas: List<Animatable<Float, AnimationVector1D>>,
    selectedWord: SingleWordData?,
    isLoading: Boolean
) {
    // Define the full screen rect in pixels
    val fullScreenRect = Rect(
        left = 0f,
        top = 0f,
        right = scale.x,
        bottom = scale.y
    )

    val region = selectedWord?.let { word ->
        val factor = if (isLoading) 3f else 1f

        val rect = word.bbox
        val centerX = (rect.left + rect.right) / 2f
        val centerY = (rect.top + rect.bottom) / 2f
        val baseRadius = Offset(
            (rect.right - rect.left) * factor,
            (rect.bottom - rect.top) * factor
        ) / 2f

        Rect(
            left = centerX - baseRadius.x,
            top = centerY - baseRadius.y,
            right = centerX + baseRadius.x,
            bottom = centerY + baseRadius.y
        )
    }

    // Choose the target rect: full screen or the provided region
    val targetRect = region ?: fullScreenRect

    // Animate the rect using animateRectAsState
    val animatedRect by animateRectAsState(
        targetValue = targetRect,
        animationSpec = tween(durationMillis = 500), // Customize as needed
    )

    val amplitudePx = with(LocalDensity.current) { 2.dp.toPx() }
    val infiniteTransition = rememberInfiniteTransition()

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val color = if (isLoading)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.surfaceBright

    Canvas(modifier = Modifier.fillMaxSize()) {
        val path = Path().apply {
            if (region != null && isLoading) {
                // Draw sine wave border around the same rectangle
                drawSineWaveBorder(
                    size = animatedRect.size,
                    amplitude = amplitudePx,
                    frequency = 5f,
                    phase = phase // animate phase as needed
                )
                translate(Offset(animatedRect.left, animatedRect.top))
            } else {
                // Draw red rectangle at animatedRect position
                addRect(
                    Rect(
                        offset = animatedRect.topLeft,
                        size = animatedRect.size
                    )
                )
            }
        }

        val scaleX = size.width / scale.x
        val scaleY = size.height / scale.y

        words.forEachIndexed { index, word ->
            val rect = word.bbox
            val alpha = alphas.getOrNull(index)?.value ?: 0f

            val left = rect.left * scaleX
            val top = rect.top * scaleY
            val right = rect.right * scaleX
            val bottom = rect.bottom * scaleY

            clipPath(path, clipOp = ClipOp.Intersect) {
                drawRoundRect(
                    color = color.copy(alpha = 0.5f * alpha),
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                    style = Fill
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

fun Path.drawSineWaveBorder(
    size: Size,
    amplitude: Float,
    frequency: Float,
    phase: Float
) {
    this.apply {
        val width = size.width
        val height = size.height

        // Top edge
        moveTo(0f, amplitude * sin(phase))
        for (x in 0..width.toInt() step 5) {
            val y = amplitude * sin(2 * PI.toFloat() * frequency * (x / width) + phase)
            lineTo(x.toFloat(), y)
        }

        // Right edge
        for (y in 0..height.toInt() step 5) {
            val x = width + amplitude * sin(2 * PI.toFloat() * frequency * (y / height) + phase)
            lineTo(x, y.toFloat())
        }

        // Bottom edge (reverse)
        for (x in width.toInt() downTo 0 step 5) {
            val y = height + amplitude * sin(2 * PI.toFloat() * frequency * (x / width) + phase)
            lineTo(x.toFloat(), y)
        }

        // Left edge (reverse)
        for (y in height.toInt() downTo 0 step 5) {
            val x = amplitude * sin(2 * PI.toFloat() * frequency * (y / height) + phase)
            lineTo(x, y.toFloat())
        }

        close()
    }
}
