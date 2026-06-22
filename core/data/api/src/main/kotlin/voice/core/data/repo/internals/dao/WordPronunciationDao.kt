package voice.core.data.repo.internals.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import voice.core.data.BookId
import voice.core.data.WordPronunciation

@Dao
public interface WordPronunciationDao {
  @Query("SELECT * FROM word_pronunciations WHERE bookId = :bookId")
  public suspend fun pronunciationsForBook(bookId: BookId): List<WordPronunciation>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public suspend fun insert(pronunciation: WordPronunciation)

  @Delete
  public suspend fun delete(pronunciation: WordPronunciation)
}
