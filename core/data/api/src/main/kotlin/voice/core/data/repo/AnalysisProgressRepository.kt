package voice.core.data.repo

import kotlinx.coroutines.flow.Flow
import voice.core.data.BookId
import voice.core.data.AnalysisProgress

public interface AnalysisProgressRepository {
  public suspend fun progressForBook(bookId: BookId): AnalysisProgress?
  public fun flowProgressForBook(bookId: BookId): Flow<AnalysisProgress?>
  public suspend fun insert(progress: AnalysisProgress)
  public suspend fun deleteForBook(bookId: BookId)
}
