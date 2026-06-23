package voice.core.data.repo.internals.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import voice.core.data.AnalysisProgress
import voice.core.data.BookId

@Dao
public interface AnalysisProgressDao {
  @Query("SELECT * FROM analysis_progress WHERE bookId = :bookId")
  public suspend fun progressForBook(bookId: BookId): AnalysisProgress?

  @Query("SELECT * FROM analysis_progress WHERE bookId = :bookId")
  public fun flowProgressForBook(bookId: BookId): Flow<AnalysisProgress?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public suspend fun insert(progress: AnalysisProgress)

  @Query("DELETE FROM analysis_progress WHERE bookId = :bookId")
  public suspend fun deleteForBook(bookId: BookId)
}
