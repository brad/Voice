package voice.core.data.repo.internals.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import voice.core.data.AudioGenerationProgress
import voice.core.data.BookId

@Dao
public interface AudioGenerationProgressDao {
  @Query("SELECT * FROM audio_generation_progress WHERE bookId = :bookId")
  public suspend fun progressForBook(bookId: BookId): AudioGenerationProgress?

  @Query("SELECT * FROM audio_generation_progress WHERE bookId = :bookId")
  public fun flowProgressForBook(bookId: BookId): Flow<AudioGenerationProgress?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public suspend fun insert(progress: AudioGenerationProgress)

  @Query("DELETE FROM audio_generation_progress WHERE bookId = :bookId")
  public suspend fun deleteForBook(bookId: BookId)
}
