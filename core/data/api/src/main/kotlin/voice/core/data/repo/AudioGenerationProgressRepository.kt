package voice.core.data.repo

import kotlinx.coroutines.flow.Flow
import voice.core.data.AudioGenerationProgress
import voice.core.data.BookId

public interface AudioGenerationProgressRepository {
  public suspend fun progressForBook(bookId: BookId): AudioGenerationProgress?
  public fun flowProgressForBook(bookId: BookId): Flow<AudioGenerationProgress?>
  public suspend fun insert(progress: AudioGenerationProgress)
  public suspend fun deleteForBook(bookId: BookId)
}
