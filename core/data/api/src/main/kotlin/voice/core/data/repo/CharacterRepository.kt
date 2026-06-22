package voice.core.data.repo

import kotlinx.coroutines.flow.Flow
import voice.core.data.BookId
import voice.core.data.Character

public interface CharacterRepository {
  public fun flowCharactersForBook(bookId: BookId): Flow<List<Character>>
  public suspend fun charactersForBook(bookId: BookId): List<Character>
  public suspend fun insert(character: Character)
  public suspend fun insertAll(characters: List<Character>)
  public suspend fun delete(character: Character)
  public suspend fun deleteForBook(bookId: BookId)
}
