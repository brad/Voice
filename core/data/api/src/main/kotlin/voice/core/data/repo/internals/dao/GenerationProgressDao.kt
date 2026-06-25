package voice.core.data.repo.internals.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import voice.core.data.BookId
import voice.core.data.GenerationProgress
import voice.core.data.GenerationStatus
import java.time.Instant

@Dao
public interface GenerationProgressDao {
  @Query("SELECT * FROM generation_progress WHERE bookId = :bookId")
  public suspend fun progressForBook(bookId: BookId): GenerationProgress?

  @Query("SELECT * FROM generation_progress WHERE bookId = :bookId")
  public fun flowProgressForBook(bookId: BookId): Flow<GenerationProgress?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public suspend fun insert(progress: GenerationProgress)

  @Query("DELETE FROM generation_progress WHERE bookId = :bookId")
  public suspend fun deleteForBook(bookId: BookId)

  @Query("UPDATE generation_progress SET status = :status, lastUpdated = :lastUpdated, errorMessage = :errorMessage WHERE bookId = :bookId")
  public suspend fun updateStatus(
    bookId: BookId,
    status: GenerationStatus,
    lastUpdated: Instant,
    errorMessage: String? = null,
  )

  @Query("UPDATE generation_progress SET povType = :povType, povCharacterName = :povCharacterName, lastUpdated = :lastUpdated WHERE bookId = :bookId")
  public suspend fun updatePov(
    bookId: BookId,
    povType: String?,
    povCharacterName: String?,
    lastUpdated: Instant,
  )

  @Query("SELECT * FROM generation_progress WHERE status != :completedStatus")
  public fun flowInProgressGenerations(completedStatus: GenerationStatus): Flow<List<GenerationProgress>>
}
