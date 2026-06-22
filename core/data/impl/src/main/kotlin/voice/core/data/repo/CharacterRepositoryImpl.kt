package voice.core.data.repo

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import voice.core.data.BookId
import voice.core.data.Character
import voice.core.data.repo.internals.dao.CharacterDao

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
public class CharacterRepositoryImpl(
  private val dao: CharacterDao,
) : CharacterRepository {
  override fun flowCharactersForBook(bookId: BookId): Flow<List<Character>> =
    dao.flowCharactersForBook(bookId)

  override suspend fun charactersForBook(bookId: BookId): List<Character> =
    dao.charactersForBook(bookId)

  override suspend fun insert(character: Character): Unit = dao.insert(character)

  override suspend fun insertAll(characters: List<Character>): Unit = dao.insertAll(characters)

  override suspend fun delete(character: Character): Unit = dao.delete(character)

  override suspend fun deleteForBook(bookId: BookId): Unit = dao.deleteForBook(bookId)
}
