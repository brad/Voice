package voice.core.work

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import voice.core.common.AppInfoProvider
import voice.core.data.repo.AnalysisProgressRepository
import voice.core.data.repo.CharacterRepository
import voice.core.data.repo.GenerationRepository
import voice.core.data.repo.NarrationPieceRepository
import voice.core.data.repo.VoiceMappingRepository
import voice.core.data.repo.WordPronunciationRepository
import voice.core.gemini.GeminiApi

@RunWith(RobolectricTestRunner::class)
class AnalysisWorkerTest {

  private lateinit var context: Context
  private val characterRepository: CharacterRepository = mockk(relaxed = true)
  private val analysisProgressRepository: AnalysisProgressRepository = mockk(relaxed = true)
  private val generationRepository: GenerationRepository = mockk(relaxed = true)
  private val voiceMappingRepository: VoiceMappingRepository = mockk(relaxed = true)
  private val wordPronunciationRepository: WordPronunciationRepository = mockk(relaxed = true)
  private val narrationPieceRepository: NarrationPieceRepository = mockk(relaxed = true)
  private val geminiApi: GeminiApi = mockk()
  private val apiKeyStore: DataStore<String> = mockk()
  private val modelStore: DataStore<String> = mockk()
  private val appInfoProvider: AppInfoProvider = mockk(relaxed = true)

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    every { apiKeyStore.data } returns flowOf("fake-api-key")
    every { modelStore.data } returns flowOf("gemini-3.1-flash-lite")
  }

  @Test
  fun `test worker failure when no bookId`() = runTest {
    val worker = TestListenableWorkerBuilder<AnalysisWorker>(context)
      .setWorkerFactory(object : androidx.work.WorkerFactory() {
        override fun createWorker(
          appContext: Context,
          workerClassName: String,
          workerParameters: WorkerParameters,
        ): ListenableWorker? {
          return AnalysisWorker(
            appContext,
            workerParameters,
            characterRepository,
            analysisProgressRepository,
            generationRepository,
            voiceMappingRepository,
            wordPronunciationRepository,
            narrationPieceRepository,
            geminiApi,
            apiKeyStore,
            modelStore,
            appInfoProvider,
          )
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
        override fun createWorker(
          appContext: Context,
          workerClassName: String,
          workerParameters: WorkerParameters,
        ): ListenableWorker? {
          return AnalysisWorker(
            appContext,
            workerParameters,
            characterRepository,
            analysisProgressRepository,
            generationRepository,
            voiceMappingRepository,
            wordPronunciationRepository,
            narrationPieceRepository,
            geminiApi,
            apiKeyStore,
            modelStore,
            appInfoProvider,
          )
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

  @Test
  fun `test SerializableCharacter decoding with missing fields`() {
    val json = """
      {
        "characters": [
          {
            "name": "John Doe"
          }
        ],
        "pronunciations": [],
        "povType": "OMNISCIENT",
        "narrationPieces": []
      }
    """
    val decoded = kotlinx.serialization.json.Json.decodeFromString<AnalysisWorker.ExtractedData>(json)
    assertEquals("John Doe", decoded.characters[0].name)
  }
}
