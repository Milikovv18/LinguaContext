package com.milikovv.linguacontext

import com.milikovv.linguacontext.test.R

import android.graphics.drawable.BitmapDrawable
import androidx.test.platform.app.InstrumentationRegistry
import com.milikovv.linguacontext.data.repo.IDetailDataItem
import com.milikovv.linguacontext.data.repo.ServiceDataItem
import com.milikovv.linguacontext.data.repo.SingleWordData
import com.milikovv.linguacontext.data.repo.WordsContainerData
import com.milikovv.linguacontext.domain.repo.WordsRepository
import com.milikovv.linguacontext.viewmodel.WordsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description


@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class BackendTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @Test
    fun `Bitmap scan`() = runTest {
        // Loading test Bitmap
        val context = InstrumentationRegistry.getInstrumentation().context
        val drawable = context.resources.getDrawable(R.drawable.book_screenshot, null)
        val bitmap = (drawable as BitmapDrawable).bitmap

        // Initializing repo
        WordsViewModel(object : WordsRepository {
            override suspend fun getServiceData(): ServiceDataItem {
                return ServiceDataItem(emptyList(), bitmap)
            }
            override suspend fun saveWordsData(data: WordsContainerData) {}
            override suspend fun getWordsData(): WordsContainerData { throw Exception() }
            override suspend fun loadDetailsData(data: List<SingleWordData>): Flow<IDetailDataItem> {
                throw Exception()
            }
        })

        // No exceptions? That's nice!
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
