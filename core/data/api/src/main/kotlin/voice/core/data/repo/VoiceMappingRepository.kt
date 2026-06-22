package voice.core.data.repo

import kotlinx.coroutines.flow.Flow
import voice.core.data.BookId
import voice.core.data.VoiceMapping
import kotlin.uuid.Uuid

public interface VoiceMappingRepository {
  public suspend fun mappingForCharacter(characterId: Uuid): VoiceMapping?
  public suspend fun mappingsForBook(bookId: BookId): List<VoiceMapping>
  public fun flowMappingsForBook(bookId: BookId): Flow<List<VoiceMapping>>
  public suspend fun insert(mapping: VoiceMapping)
  public suspend fun insertAll(mappings: List<VoiceMapping>)
  public suspend fun delete(mapping: VoiceMapping)
  public suspend fun deleteForBook(bookId: BookId)
}
