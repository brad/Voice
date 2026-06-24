package voice.core.work

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import voice.core.data.BookId

public interface AudiobookGenerationManager {
  public fun generateAudiobook(bookId: BookId)
}

@ContributesBinding(AppScope::class)
public class WorkManagerAudiobookGenerationManager(private val workManager: WorkManager) : AudiobookGenerationManager {

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

  private fun uniqueWorkName(bookId: BookId): String {
    return "audiobook-generation-${bookId.value}"
  }
}
