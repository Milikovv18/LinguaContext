package com.milikovv.linguacontext.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.Bitmap
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.milikovv.linguacontext.data.repo.NodeInfoData
import com.milikovv.linguacontext.data.repo.ServiceDataItem
import com.milikovv.linguacontext.domain.repo.ServiceRepository
import com.milikovv.linguacontext.ui.WordsActivity
import com.milikovv.linguacontext.utils.bounds
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.coroutines.resumeWithException

/**
 * System service that provides access to screen contents in a textual and visual form.
 * The class is created by system whenever AS shortcut is used, so it guarantees screen contents
 * access only when user requests it explicitly.<br>
 * Textual info extracted via [getFlatNodeInfoList], screenshots are retrieved
 * with [takeScreenshotSuspend].
 */
@AndroidEntryPoint
class ScreenReader : AccessibilityService() {
    // Custom coroutine scope is need here, because AccessibilityService doesn't provide one
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    @Inject // Repo to write collected data to
    lateinit var repo: ServiceRepository

    /**
     * Unused in this app and is never called
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        TODO("Not yet implemented")
    }

    /**
     * Start collecting data immediately after service connected event (e.g. on shortcut click).
     */
    override fun onServiceConnected() {
        super.onServiceConnected()

        serviceInfo.apply {
            eventTypes = 0
        }

        serviceScope.launch {
            // Launch both suspend functions concurrently using async
            val screenshotDeferred = async { takeScreenshotSuspend() }
            val nodeListDeferred = async { getFlatNodeInfoList() }

            // Await both results concurrently
            repo.saveServiceData(ServiceDataItem(nodeListDeferred.await(),
                screenshotDeferred.await()))

            // Start activity
            val intent = Intent(applicationContext, WordsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    /**
     * Flattens [AccessibilityNodeInfo] to a list of easily processable [NodeInfoData].
     * Filters out nodes with empty text field.
     * @return flattened list
     */
    private suspend fun getFlatNodeInfoList() : List<NodeInfoData> =
        withContext(Dispatchers.Default) {
            val list = mutableListOf<NodeInfoData>()

            fun traverse(node: AccessibilityNodeInfo?) {
                if (node == null) return

                // Add current node info to list
                node.text?.toString()?.let {
                    if (!it.isBlank())
                        list.add(NodeInfoData(it, node.bounds))
                }

                // Recurse for children
                for (i in 0 until node.childCount) {
                    traverse(node.getChild(i))
                }
            }

            traverse(rootInActiveWindow)
            list
        }

    /**
     * Retrieves screenshot in a form of [Bitmap].<br>
     * Calls [takeScreenshot] under the hood. Wraps callback into [suspendCancellableCoroutine].
     * @return screenshot image data
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun takeScreenshotSuspend() : Bitmap =
        suspendCancellableCoroutine { cont ->
            takeScreenshot(
                Display.DEFAULT_DISPLAY,
                Executors.newSingleThreadExecutor(),
                object : TakeScreenshotCallback {
                    override fun onSuccess(screenshotResult: ScreenshotResult) {
                        try {
                            val hardwareBuffer = screenshotResult.hardwareBuffer
                            val colorSpace = screenshotResult.colorSpace
                            val bitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, colorSpace)
                            hardwareBuffer.close() // Free resources
                            if (bitmap != null) {
                                cont.resume(bitmap) { _, _, _ -> }
                            } else {
                                cont.resumeWithException(IllegalStateException("Bitmap is null"))
                            }
                        } catch (e: Exception) {
                            cont.resumeWithException(e)
                        }
                    }

                    override fun onFailure(errorCode: Int) {
                        cont.resumeWithException(RuntimeException("Screenshot failed with code $errorCode"))
                    }
                }
            )
        }


    override fun onInterrupt() {
        TODO("Not yet implemented")
    }

    /**
     * Requests app shutdown via [WordsActivity.CLOSE_INTENT]. E.g. on shortcut click.
     */
    override fun onDestroy() {
        // Closing Activity when done
        val intent = Intent(WordsActivity.CLOSE_INTENT)
            .setPackage(applicationContext.packageName)
        applicationContext.sendBroadcast(intent)

        super.onDestroy()
    }
}