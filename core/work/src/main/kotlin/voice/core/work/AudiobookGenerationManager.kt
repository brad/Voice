package voice.core.work

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import java.io.File
import java.time.Instant
import voice.core.data.BookId
import voice.core.data.GenerationProgress
import voice.core.data.GenerationStatus
import voice.core.data.repo.AudioGenerationProgressRepository
import voice.core.data.repo.GenerationRepository

public interface AudiobookGenerationManager {
  public fun generateAudiobook(bookId: BookId)
  public fun cancelGeneration(bookId: BookId)
  public suspend fun discardGeneration(bookId: BookId)
}

@ContributesBinding(AppScope::class)
public class WorkManagerAudiobookGenerationManager(
  private val context: Context,
  private val workManager: WorkManager,
  private val audioGenerationProgressRepository: AudioGenerationProgressRepository,
  private val generationRepository: GenerationRepository,
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

  private fun uniqueWorkName(bookId: BookId): String {
    return "audiobook-generation-${bookId.value}"
  }
}
