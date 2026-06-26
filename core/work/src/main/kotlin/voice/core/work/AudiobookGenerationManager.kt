package voice.core.work

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import voice.core.data.BookId
import voice.core.data.GenerationProgress
import voice.core.data.GenerationStatus
import voice.core.data.repo.AnalysisProgressRepository
import voice.core.data.repo.AudioGenerationProgressRepository
import voice.core.data.repo.CharacterRepository
import voice.core.data.repo.GenerationRepository
import voice.core.data.repo.VoiceMappingRepository
import voice.core.data.repo.WordPronunciationRepository
import java.io.File
import java.time.Instant

public interface AudiobookGenerationManager {
  public fun generateAudiobook(bookId: BookId)
  public fun cancelGeneration(bookId: BookId)
  public suspend fun discardGeneration(bookId: BookId)
  public suspend fun cancelImport(bookId: BookId)
  public suspend fun retry(bookId: BookId)
}

@ContributesBinding(AppScope::class)
public class WorkManagerAudiobookGenerationManager(
  private val context: Context,
  private val workManager: WorkManager,
  private val audioGenerationProgressRepository: AudioGenerationProgressRepository,
  private val generationRepository: GenerationRepository,
  private val analysisProgressRepository: AnalysisProgressRepository,
  private val characterRepository: CharacterRepository,
  private val voiceMappingRepository: VoiceMappingRepository,
  private val wordPronunciationRepository: WordPronunciationRepository,
) : AudiobookGenerationManager {

  public override fun generateAudiobook(bookId: BookId) {
    val request = OneTimeWorkRequestBuilder<GenerationWorker>()
      .setInputData(workDataOf(GenerationWorker.KEY_BOOK_ID to bookId.value))
      .build()

    workManager.enqueueUniqueWork(
      uniqueWorkName(bookId),
      ExistingWorkPolicy.KEEP,
      request,
    )
  }

  public override fun cancelGeneration(bookId: BookId) {
    workManager.cancelUniqueWork(uniqueWorkName(bookId))
  }

  public override suspend fun discardGeneration(bookId: BookId) {
    cancelGeneration(bookId)
    audioGenerationProgressRepository.deleteForBook(bookId)
    generationRepository.insert(
      GenerationProgress(
        bookId = bookId,
        status = GenerationStatus.ANALYZED,
        lastUpdated = Instant.now(),
      ),
    )
    val outputDir = File(context.filesDir, "audiobooks/${bookId.value}")
    outputDir.deleteRecursively()
  }

  public override suspend fun cancelImport(bookId: BookId) {
    workManager.cancelUniqueWork("epub-analysis-${bookId.value}")
    cancelGeneration(bookId)

    generationRepository.deleteForBook(bookId)
    audioGenerationProgressRepository.deleteForBook(bookId)
    analysisProgressRepository.deleteForBook(bookId)
    characterRepository.deleteForBook(bookId)
    voiceMappingRepository.deleteForBook(bookId)
    wordPronunciationRepository.deleteForBook(bookId)

    val outputDir = File(context.filesDir, "audiobooks/${bookId.value}")
    outputDir.deleteRecursively()
  }

  public override suspend fun retry(bookId: BookId) {
    val progress = generationRepository.progressForBook(bookId) ?: return
    if (progress.status != GenerationStatus.FAILED) return

    val analysisProgress = analysisProgressRepository.progressForBook(bookId)
    val isAnalysisComplete = analysisProgress != null && analysisProgress.currentChunkIndex >= analysisProgress.totalChunks

    if (!isAnalysisComplete) {
      val request = OneTimeWorkRequestBuilder<AnalysisWorker>()
        .setInputData(workDataOf(AnalysisWorker.KEY_BOOK_ID to bookId.value))
        .build()
      workManager.enqueueUniqueWork(
        "epub-analysis-${bookId.value}",
        ExistingWorkPolicy.REPLACE,
        request,
      )
    } else {
      val request = OneTimeWorkRequestBuilder<GenerationWorker>()
        .setInputData(workDataOf(GenerationWorker.KEY_BOOK_ID to bookId.value))
        .build()
      workManager.enqueueUniqueWork(
        uniqueWorkName(bookId),
        ExistingWorkPolicy.REPLACE,
        request,
      )
    }
  }

  private fun uniqueWorkName(bookId: BookId): String {
    return "audiobook-generation-${bookId.value}"
  }
}
