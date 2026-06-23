package voice.core.work

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.WorkManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test
import voice.core.data.BookId

class EpubImportManagerTest {

  @Test
  fun `import epub enqueues analysis worker with book id`() {
    val request = slot<OneTimeWorkRequest>()
    val workManager = mockk<WorkManager>()
    every {
      workManager.enqueueUniqueWork(any(), any(), capture(request))
    } returns mockk<Operation>(relaxed = true)
    val manager = WorkManagerEpubImportManager(workManager)
    val bookId = BookId("content://books/import.epub")

    manager.importEpub(bookId)

    verify(exactly = 1) {
      workManager.enqueueUniqueWork(
        "epub-analysis-${bookId.value}",
        ExistingWorkPolicy.KEEP,
        any(),
      )
    }
    assertEquals(
      bookId.value,
      request.captured.workSpec.input.getString(AnalysisWorker.KEY_BOOK_ID),
    )
  }
}
