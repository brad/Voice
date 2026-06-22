package voice.core.data.repo.internals.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import voice.core.data.BookId
import voice.core.data.GenerationProgress

@Dao
public interface GenerationProgressDao {
  @Query("SELECT * FROM generation_progress WHERE bookId = :bookId")
  public suspend fun progressForBook(bookId: BookId): GenerationProgress?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public suspend fun insert(progress: GenerationProgress)

  @Query("DELETE FROM generation_progress WHERE bookId = :bookId")
  public suspend fun deleteForBook(bookId: BookId)
}
