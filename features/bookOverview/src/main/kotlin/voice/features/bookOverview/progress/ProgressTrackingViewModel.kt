package voice.features.bookOverview.progress

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.content.FileProvider
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import voice.core.data.BookId
import voice.core.data.GenerationStatus
import voice.core.data.repo.AnalysisProgressRepository
import voice.core.data.repo.AudioGenerationProgressRepository
import voice.core.data.repo.GenerationRepository
import voice.core.work.AudiobookGenerationManager
import voice.navigation.Destination
import voice.navigation.Navigator
import java.io.File
import java.time.Instant

@Inject
class ProgressTrackingViewModel(
  private val context: Context,
  private val generationRepository: GenerationRepository,
  private val analysisProgressRepository: AnalysisProgressRepository,
  private val audioGenerationProgressRepository: AudioGenerationProgressRepository,
  private val audiobookGenerationManager: AudiobookGenerationManager,
  private val navigator: Navigator,
) {

  @Composable
  internal fun state(bookId: BookId): ProgressTrackingViewState {
    val generationProgress by remember(bookId) {
      generationRepository.flowProgressForBook(bookId)
    }.collectAsState(initial = null)
    val analysisProgress by remember(bookId) {
      analysisProgressRepository.flowProgressForBook(bookId)
    }.collectAsState(initial = null)
    val audioProgress by remember(bookId) {
      audioGenerationProgressRepository.flowProgressForBook(bookId)
    }.collectAsState(initial = null)

    return ProgressTrackingViewState(
      status = generationProgress?.status,
      analysisCurrentChunk = analysisProgress?.currentChunkIndex,
      analysisTotalChunks = analysisProgress?.totalChunks,
      generationChapter = audioProgress?.chapterIndex?.plus(1),
      generationChunk = audioProgress?.chunkIndex,
      generationTotalChunks = audioProgress?.totalChunks,
      errorMessage = generationProgress?.errorMessage,
      lastUpdated = listOfNotNull(
        generationProgress?.lastUpdated,
        analysisProgress?.lastUpdated,
        audioProgress?.lastUpdated,
      ).maxOrNull(),
    )
  }

  fun onConfigureGeneration(bookId: BookId) {
    navigator.goTo(Destination.GenerationSettings(bookId))
  }

  fun onRetry(bookId: BookId, scope: kotlinx.coroutines.CoroutineScope) {
    scope.launch {
      audiobookGenerationManager.retry(bookId)
    }
  }

  fun shareError(errorMessage: String): Uri {
    val logsDir = File(context.cacheDir, "logs").apply { mkdirs() }
    val logFile = File(logsDir, "error_log_${System.currentTimeMillis()}.log")
    logFile.writeText(errorMessage)

    return FileProvider.getUriForFile(
      context,
      "${context.packageName}.coverprovider",
      logFile
    )
  }

  fun close() {
    navigator.goBack()
  }
}

internal data class ProgressTrackingViewState(
  val status: GenerationStatus?,
  val analysisCurrentChunk: Int?,
  val analysisTotalChunks: Int?,
  val generationChapter: Int?,
  val generationChunk: Int?,
  val generationTotalChunks: Int?,
  val errorMessage: String?,
  val lastUpdated: Instant?,
)
