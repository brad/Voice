package voice.core.data.repo.internals.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import voice.core.data.BookId
import voice.core.data.Character
import kotlin.uuid.Uuid

@Dao
public interface CharacterDao {
  @Query("SELECT * FROM characters WHERE bookId = :bookId")
  public suspend fun charactersForBook(bookId: BookId): List<Character>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public suspend fun insert(character: Character)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public suspend fun insertAll(characters: List<Character>)

  @Delete
  public suspend fun delete(character: Character)

  @Query("DELETE FROM characters WHERE bookId = :bookId")
  public suspend fun deleteForBook(bookId: BookId)
}
