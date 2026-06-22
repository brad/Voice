package voice.core.data.repo

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import voice.core.data.BookId
import voice.core.data.VoiceMapping
import voice.core.data.repo.internals.dao.VoiceMappingDao
import kotlin.uuid.Uuid

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
public class VoiceMappingRepositoryImpl(
  private val dao: VoiceMappingDao,
) : VoiceMappingRepository {
  override suspend fun mappingForCharacter(characterId: Uuid): VoiceMapping? =
    dao.mappingForCharacter(characterId)

  override suspend fun mappingsForBook(bookId: BookId): List<VoiceMapping> =
    dao.mappingsForBook(bookId)

  override fun flowMappingsForBook(bookId: BookId): Flow<List<VoiceMapping>> =
    dao.flowMappingsForBook(bookId)

  override suspend fun insert(mapping: VoiceMapping): Unit = dao.insert(mapping)

  override suspend fun insertAll(mappings: List<VoiceMapping>): Unit = dao.insertAll(mappings)

  override suspend fun delete(mapping: VoiceMapping): Unit = dao.delete(mapping)

  override suspend fun deleteForBook(bookId: BookId): Unit = dao.deleteForBook(bookId)
}
