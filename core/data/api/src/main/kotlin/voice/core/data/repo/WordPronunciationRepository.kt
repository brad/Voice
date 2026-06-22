package voice.core.data.repo

import kotlinx.coroutines.flow.Flow
import voice.core.data.BookId
import voice.core.data.WordPronunciation

public interface WordPronunciationRepository {
  public suspend fun pronunciationsForBook(bookId: BookId): List<WordPronunciation>
  public fun flowPronunciationsForBook(bookId: BookId): Flow<List<WordPronunciation>>
  public suspend fun insert(pronunciation: WordPronunciation)
  public suspend fun insertAll(pronunciations: List<WordPronunciation>)
  public suspend fun delete(pronunciation: WordPronunciation)
  public suspend fun deleteForBook(bookId: BookId)
}
