package voice.core.work

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import voice.core.data.BookId

public interface EpubImportManager {
  public fun importEpub(bookId: BookId)
}

@ContributesBinding(AppScope::class)
public class WorkManagerEpubImportManager(private val workManager: WorkManager) : EpubImportManager {

  public override fun importEpub(bookId: BookId) {
    val request = OneTimeWorkRequestBuilder<AnalysisWorker>()
      .setInputData(workDataOf(AnalysisWorker.KEY_BOOK_ID to bookId.value))
      .build()

    workManager.enqueueUniqueWork(
      uniqueWorkName(bookId),
      ExistingWorkPolicy.KEEP,
      request,
    )
  }

  private fun uniqueWorkName(bookId: BookId): String {
    return "epub-analysis-${bookId.value}"
  }
}
