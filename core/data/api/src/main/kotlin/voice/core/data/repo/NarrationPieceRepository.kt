package voice.core.data.repo

import voice.core.data.BookId
import voice.core.data.NarrationPiece

public interface NarrationPieceRepository {
  public suspend fun insertAll(pieces: List<NarrationPiece>)
  public suspend fun deleteForBook(bookId: BookId)
  public suspend fun getForBook(bookId: BookId): List<NarrationPiece>
}
