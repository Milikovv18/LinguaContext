package com.milikovv.linguacontext.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.milikovv.linguacontext.data.repo.ExplanationDetail

@Composable
fun OllamaResponseView(
    item: ExplanationDetail,
    onSkipThink: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val startTime = remember { System.currentTimeMillis() }
    var currentTime by remember { mutableLongStateOf(0L) }

    if (item.isThinking)
        currentTime = System.currentTimeMillis() - startTime

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            ThinkButton(
                item.isThinking,
                currentTime,
            ) {
                expanded = !expanded
            }

            if (item.isThinking)
                Text(
                    text = "skip",
                    color = Color.LightGray,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { onSkipThink() }
                )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Reveal text below button, animates size change smoothly
        if (expanded)
            ThinkableText(item.thinkingText)
        AnswerText(item.answerText)
    }
}

@Composable
fun ThinkButton(
    isThinking: Boolean,
    thinkingTime: Long,
    onExpandClick: () -> Unit
) {
    // When expanded == true, stop animation by fixing the translate value at 0f
    val translateAnim = if (isThinking) {
        // Run infinite shimmer animation
        val transition = rememberInfiniteTransition()
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    } else {
        // Animation stopped, fixed value
        animateFloatAsState(targetValue = 0f)
    }

    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = translateAnim.value - 200f, y = 0f),
        end = Offset(x = translateAnim.value, y = 0f)
    )

    Button(
        onClick = onExpandClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.DarkGray
        ),
        modifier = Modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(brush, RoundedCornerShape(12.dp))
    ) {
        val amountOfTime = "%.1fs".format(thinkingTime / 1000f)
        if (isThinking)
            Text(text = "Thinking... $amountOfTime")
        else
            Text(text = "Thought for $amountOfTime")
    }
}

@Composable
fun ThinkableText(thinkText: String) {
    Text(
        text = if (thinkText.isNotBlank()) "$thinkText •" else "",
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        color = Color.LightGray
    )
}

@Composable
fun AnswerText(answer: String) {
    Text(
        text = if (answer.isNotBlank()) "$answer •" else "",
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        color = Color.DarkGray
    )
}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    JetpackTestTheme {
//        ShimmerSkeletonButton(null)
//    }
//}