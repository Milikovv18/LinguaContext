package com.milikovv.linguacontext.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.milikovv.linguacontext.data.repo.ExplanationDetail
import com.milikovv.linguacontext.data.repo.FormalityDetail
import com.milikovv.linguacontext.data.repo.IDetailDataItem
import com.milikovv.linguacontext.data.repo.WordDetail
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.composables.core.DragIndication
import com.composables.core.ModalBottomSheet
import com.composables.core.ModalBottomSheetState
import com.composables.core.Sheet
import com.composables.core.SheetDetent
import com.milikovv.linguacontext.R
import com.milikovv.linguacontext.ui.theme.LinguaContextTheme


@Composable
fun BottomHeader(item: WordDetail) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        itemVerticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(item.word, style = MaterialTheme.typography.titleMedium)
        if (item.phonetic != null)
            Text(item.phonetic, style = MaterialTheme.typography.titleSmall, color = Color.LightGray)
    }
}

@Composable
fun FormalityBar(item: FormalityDetail) {
    Text(
        stringResource(R.string.formality),
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    var progressTarget by remember { mutableFloatStateOf(0f) }

    // Trigger animation after first composition
    LaunchedEffect(Unit) {
        progressTarget = item.formality.coerceIn(0f, 1f)
    }

    // Animate the progress value from 0f to targetValue (0..1)
    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = 1000) // 1 second animation
    )

    // The bar container
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .background(Color.LightGray, shape = RoundedCornerShape(12.dp))
    ) {
        // The animated fill bar
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .background(Color.Blue, shape = RoundedCornerShape(12.dp))
        )
    }
}


val Peek = SheetDetent(
    identifier = "peek"
) { containerHeight, sheetHeight ->
    containerHeight * 0.2f  // At the very bottom
}

val Half = SheetDetent(
    identifier = "half"
) { containerHeight, sheetHeight ->
    containerHeight * 0.6f  // 60% of the screen height
}

val Full90 = SheetDetent(
    identifier = "full90"
) { containerHeight, sheetHeight ->
    containerHeight * 0.9f  // 90% of the screen height
}


@Composable
fun BottomSheetStructure(
    sheetState: ModalBottomSheetState,
    detailData: List<IDetailDataItem>,
    isLoading: Boolean,
    error: String?,
    onDismissSheet: () -> Unit,
    onSkipThink: () -> Unit
) {
    ModalBottomSheet(
        onDismiss = onDismissSheet,
        state = sheetState
    ) {
        Sheet(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color.White)
                    .height(1200.dp),  // content height
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DragIndication(
                        modifier = Modifier
                            .padding(top = 22.dp)
                            .background(Color.Black.copy(0.4f), RoundedCornerShape(100))
                            .width(32.dp)
                            .height(4.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    BottomSheetContent(
                        availItems = detailData,
                        isLoading = isLoading,
                        onSkipThink = onSkipThink,
                        error = error
                    )
                }
            }
        }
    }
}


@Composable
fun BottomSheetContent(
    availItems: List<IDetailDataItem>,
    isLoading: Boolean,
    onSkipThink: () -> Unit,
    error: String? = null
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        if (isLoading && availItems.isEmpty()) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                items(availItems) { item ->
                    when (item) {
                        is WordDetail -> BottomHeader(item)
                        is ExplanationDetail -> OllamaResponseView(item, isLoading, onSkipThink)
                        is FormalityDetail -> FormalityBar(item)
                    }
                }
                if (isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
        error?.let { errorMsg ->
            Text(text = "Error: $errorMsg", color = Color.Red)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun BottomSheetPreview() {
    LinguaContextTheme {
        BottomSheetContent(listOf(
            WordDetail("Hello", "hello"),
            ExplanationDetail("Kinda long explanation here just for multiline content. Kinda long explanation here just for multiline content", "", 0L, false),
            FormalityDetail(0.5f)
        ), false, {})
    }
}
