package voice.core.work

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import voice.core.data.repo.AnalysisProgressRepository
import voice.core.data.repo.CharacterRepository
import voice.core.gemini.GeminiApi
import io.mockk.every

@RunWith(RobolectricTestRunner::class)
class AnalysisWorkerTest {

  private lateinit var context: Context
  private val characterRepository: CharacterRepository = mockk(relaxed = true)
  private val analysisProgressRepository: AnalysisProgressRepository = mockk(relaxed = true)
  private val geminiApi: GeminiApi = mockk()
  private val apiKeyStore: DataStore<String> = mockk()
  private val modelStore: DataStore<String> = mockk()

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    every { apiKeyStore.data } returns flowOf("fake-api-key")
    every { modelStore.data } returns flowOf("gemini-1.5-flash")
  }

  @Test
  fun `test worker failure when no bookId`() = runTest {
    val worker = TestListenableWorkerBuilder<AnalysisWorker>(context)
      .setWorkerFactory(object : androidx.work.WorkerFactory() {
        override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? {
          return AnalysisWorker(appContext, workerParameters, characterRepository, analysisProgressRepository, geminiApi, apiKeyStore, modelStore)
        }
      })
      .build()

    val result = worker.doWork()
    assertEquals(ListenableWorker.Result.failure(), result)
  }

  @Test
  fun `test worker failure when book not found`() = runTest {
    val worker = TestListenableWorkerBuilder<AnalysisWorker>(context)
      .setWorkerFactory(object : androidx.work.WorkerFactory() {
        override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? {
          return AnalysisWorker(appContext, workerParameters, characterRepository, analysisProgressRepository, geminiApi, apiKeyStore, modelStore)
        }
      })
      .setInputData(workDataOf(AnalysisWorker.KEY_BOOK_ID to "content://non.existent/book.epub"))
      .build()

    val result = try {
       worker.doWork()
    } catch (e: Exception) {
       ListenableWorker.Result.failure()
    }
    assertEquals(ListenableWorker.Result.failure(), result)
  }
}
