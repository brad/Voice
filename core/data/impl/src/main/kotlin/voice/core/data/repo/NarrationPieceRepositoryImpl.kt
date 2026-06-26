package voice.core.data.repo

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import voice.core.data.BookId
import voice.core.data.NarrationPiece
import voice.core.data.repo.internals.dao.NarrationPieceDao

@ContributesBinding(AppScope::class)
public class NarrationPieceRepositoryImpl(private val dao: NarrationPieceDao) : NarrationPieceRepository {
  override suspend fun insertAll(pieces: List<NarrationPiece>) {
    dao.insertAll(pieces)
  }

  override suspend fun deleteForBook(bookId: BookId) {
    dao.deleteForBook(bookId)
  }

  override suspend fun getForBook(bookId: BookId): List<NarrationPiece> {
    return dao.getForBook(bookId)
  }
}
