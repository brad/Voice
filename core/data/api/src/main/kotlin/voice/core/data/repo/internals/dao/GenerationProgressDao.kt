package voice.core.data.repo.internals.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import voice.core.data.BookId
import java.time.Instant
import voice.core.data.GenerationProgress
import voice.core.data.GenerationStatus

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
  @Query("UPDATE generation_progress SET status = :status, lastUpdated = :lastUpdated WHERE bookId = :bookId")
  public suspend fun updateStatus(bookId: BookId, status: GenerationStatus, lastUpdated: Instant)

  @Query("SELECT * FROM generation_progress WHERE status != :completedStatus")
  public fun flowInProgressGenerations(completedStatus: GenerationStatus): Flow<List<GenerationProgress>>
}
