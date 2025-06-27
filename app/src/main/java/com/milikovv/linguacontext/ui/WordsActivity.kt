package com.milikovv.linguacontext.ui

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.PointF
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.milikovv.linguacontext.viewmodel.WordsViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.milikovv.linguacontext.data.repo.DetailsState
import com.milikovv.linguacontext.data.repo.SingleWordData
import com.milikovv.linguacontext.ui.theme.LinguaContextTheme
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WordsActivity : ComponentActivity() {
    private val viewModel: WordsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LinguaContextTheme {
                val wordsState by viewModel.serviceState.collectAsState()

                // Skipping composition until data is available
                val data = wordsState.data
                if (data == null) {
                    LoadingIndicator()
                    return@LinguaContextTheme
                }

                val words = data.words
                val image = data.background

                Box(modifier = Modifier.fillMaxSize()) {
                    // Main UI content here
                    val selected = remember(words) { words.find { it.selected } }
                    MainScreen(
                        image = image,
                        words = words,
                        selectedWord = selected,
                        detailsState = viewModel.detailsState.collectAsState().value,
                        onWordSelected = {
                            viewModel.selectWord(it)
                            viewModel.requestAnalysis()
                        },
                        onDismissSheet = {
                            viewModel.selectWord(null)
                            viewModel.stopAnalysis()
                        }
                    )
                }
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d(TAG, "Broadcast registered")
            registerReceiver(closeReceiver, IntentFilter(CLOSE_INTENT), RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(closeReceiver, IntentFilter(CLOSE_INTENT))
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "Broadcast unregistered")
        unregisterReceiver(closeReceiver)
    }


    private val closeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == CLOSE_INTENT) {
                finish()
            }
        }
    }

    companion object {
        val TAG = WordsActivity::class.simpleName
        const val CLOSE_INTENT = "ACTIVITY_CLOSE_REQUEST"
    }
}


@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    image: Bitmap,
    words: List<SingleWordData>,
    selectedWord: SingleWordData?,
    detailsState: DetailsState,
    onWordSelected: (SingleWordData) -> Unit,
    onDismissSheet: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    // Safely get sheet offset; fallback to 0 if not available
    val offsetPx = runCatching { sheetState.requireOffset().toInt() }.getOrDefault(0)

    // Control showing/hiding sheet when selectedWord changes
    LaunchedEffect(selectedWord) {
        if (selectedWord != null) {
            showSheet = true
            scope.launch { sheetState.show() }
        } else {
            scope.launch { sheetState.hide() }
            showSheet = false
        }
    }

    val canvasOffset = if (selectedWord != null && offsetPx in 1..selectedWord.bbox.bottom) {
        offsetPx - selectedWord.bbox.bottom - selectedWord.bbox.height()
    } else {
        -image.height
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        translate(top = canvasOffset.toFloat()) {
            drawImage(
                image = image.asImageBitmap(),
                dstSize = IntSize(size.width.toInt(), size.height.toInt())
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    onDismissSheet()
                    showSheet = false
                },
                sheetState = sheetState,
            ) {
                BottomSheetContent(
                    detailsState.detailData,
                    detailsState.isLoading,
                    detailsState.error
                )
            }
        }

        val rectOffset = if (selectedWord != null && offsetPx < selectedWord.bbox.bottom)
            canvasOffset - image.height
        else
            canvasOffset

        RectanglesOverlay(
            modifier = Modifier.offset { IntOffset(0, rectOffset.toInt() + image.height) },
            scale = PointF(image.width.toFloat(), image.height.toFloat()),
            words = words,
            selectedWord = selectedWord,
            isLoading = detailsState.isLoading,
            onRectClick = onWordSelected
        )
    }
}
