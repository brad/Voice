package voice.features.bookOverview.progress

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dev.zacsweers.metro.Inject
import voice.core.data.BookId
import voice.core.data.GenerationStatus
import voice.core.data.repo.AnalysisProgressRepository
import voice.core.data.repo.AudioGenerationProgressRepository
import voice.core.data.repo.GenerationRepository
import voice.navigation.Navigator
import java.time.Instant

@Inject
class ProgressTrackingViewModel(
  private val generationRepository: GenerationRepository,
  private val analysisProgressRepository: AnalysisProgressRepository,
  private val audioGenerationProgressRepository: AudioGenerationProgressRepository,
  private val navigator: Navigator,
) {

  @Composable
  fun state(bookId: BookId): ProgressTrackingViewState {
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
      lastUpdated = listOfNotNull(
        generationProgress?.lastUpdated,
        analysisProgress?.lastUpdated,
        audioProgress?.lastUpdated,
      ).maxOrNull(),
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
  val lastUpdated: Instant?,
)
