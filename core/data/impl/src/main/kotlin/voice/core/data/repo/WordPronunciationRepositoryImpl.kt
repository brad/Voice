package voice.core.data.repo

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import voice.core.data.BookId
import voice.core.data.WordPronunciation
import voice.core.data.repo.internals.dao.WordPronunciationDao

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
public class WordPronunciationRepositoryImpl(
  private val dao: WordPronunciationDao,
) : WordPronunciationRepository {
  override suspend fun pronunciationsForBook(bookId: BookId): List<WordPronunciation> =
    dao.pronunciationsForBook(bookId)

  override fun flowPronunciationsForBook(bookId: BookId): Flow<List<WordPronunciation>> =
    dao.flowPronunciationsForBook(bookId)

  override suspend fun insert(pronunciation: WordPronunciation): Unit = dao.insert(pronunciation)

  override suspend fun insertAll(pronunciations: List<WordPronunciation>): Unit = dao.insertAll(pronunciations)

  override suspend fun delete(pronunciation: WordPronunciation): Unit = dao.delete(pronunciation)

  override suspend fun deleteForBook(bookId: BookId): Unit = dao.deleteForBook(bookId)
}
