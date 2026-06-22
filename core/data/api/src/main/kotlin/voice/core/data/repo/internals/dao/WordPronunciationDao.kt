package voice.core.data.repo.internals.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import voice.core.data.BookId
import voice.core.data.WordPronunciation

@Dao
public interface WordPronunciationDao {
  @Query("SELECT * FROM word_pronunciations WHERE bookId = :bookId")
  public suspend fun pronunciationsForBook(bookId: BookId): List<WordPronunciation>

  @Query("SELECT * FROM word_pronunciations WHERE bookId = :bookId")
  public fun flowPronunciationsForBook(bookId: BookId): Flow<List<WordPronunciation>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public suspend fun insert(pronunciation: WordPronunciation)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public suspend fun insertAll(pronunciations: List<WordPronunciation>)

  @Delete
  public suspend fun delete(pronunciation: WordPronunciation)

  @Query("DELETE FROM word_pronunciations WHERE bookId = :bookId")
  public suspend fun deleteForBook(bookId: BookId)
}
