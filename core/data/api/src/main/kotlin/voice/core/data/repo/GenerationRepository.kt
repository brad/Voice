package voice.core.data.repo

import kotlinx.coroutines.flow.Flow
import voice.core.data.BookId
import voice.core.data.GenerationProgress

public interface GenerationRepository {
  public suspend fun progressForBook(bookId: BookId): GenerationProgress?
  public fun flowProgressForBook(bookId: BookId): Flow<GenerationProgress?>
  public suspend fun insert(progress: GenerationProgress)
  public suspend fun deleteForBook(bookId: BookId)
}
