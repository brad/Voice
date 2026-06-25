package voice.core.data.repo

import kotlinx.coroutines.flow.Flow
import voice.core.data.BookId
import voice.core.data.GenerationProgress
import voice.core.data.GenerationStatus
import voice.core.data.PovType
import java.time.Instant

public interface GenerationRepository {
  public suspend fun progressForBook(bookId: BookId): GenerationProgress?
  public fun flowProgressForBook(bookId: BookId): Flow<GenerationProgress?>
  public suspend fun insert(progress: GenerationProgress)
  public suspend fun deleteForBook(bookId: BookId)
  public suspend fun updatePov(
    bookId: BookId,
    povType: PovType,
    povCharacterName: String?,
  )
  public suspend fun updateStatus(
    bookId: BookId,
    status: GenerationStatus,
    lastUpdated: Instant,
    errorMessage: String? = null,
  )
  public fun flowInProgressGenerations(): Flow<List<GenerationProgress>>
}
