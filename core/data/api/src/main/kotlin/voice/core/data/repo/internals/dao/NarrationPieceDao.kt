package voice.core.data.repo.internals.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import voice.core.data.BookId
import voice.core.data.NarrationPiece

@Dao
public interface NarrationPieceDao {
  @Insert
  public suspend fun insertAll(pieces: List<NarrationPiece>)

  @Query("DELETE FROM narration_pieces WHERE bookId = :bookId")
  public suspend fun deleteForBook(bookId: BookId)

  @Query("SELECT * FROM narration_pieces WHERE bookId = :bookId ORDER BY `index` ASC")
  public suspend fun getForBook(bookId: BookId): List<NarrationPiece>
}
