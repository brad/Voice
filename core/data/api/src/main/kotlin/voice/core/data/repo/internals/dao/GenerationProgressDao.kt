package voice.core.data.repo.internals.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import voice.core.data.BookId
import voice.core.data.GenerationProgress

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
}
