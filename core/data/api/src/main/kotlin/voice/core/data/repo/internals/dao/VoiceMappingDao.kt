package voice.core.data.repo.internals.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import voice.core.data.BookId
import voice.core.data.VoiceMapping
import kotlin.uuid.Uuid

@Dao
public interface VoiceMappingDao {
  @Query("SELECT * FROM voice_mappings WHERE characterId = :characterId")
  public suspend fun mappingForCharacter(characterId: Uuid): VoiceMapping?

  @Query("SELECT vm.* FROM voice_mappings vm INNER JOIN characters c ON vm.characterId = c.id WHERE c.bookId = :bookId")
  public suspend fun mappingsForBook(bookId: BookId): List<VoiceMapping>

  @Query("SELECT vm.* FROM voice_mappings vm INNER JOIN characters c ON vm.characterId = c.id WHERE c.bookId = :bookId")
  public fun flowMappingsForBook(bookId: BookId): Flow<List<VoiceMapping>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public suspend fun insert(mapping: VoiceMapping)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public suspend fun insertAll(mappings: List<VoiceMapping>)

  @Delete
  public suspend fun delete(mapping: VoiceMapping)

  @Query("DELETE FROM voice_mappings WHERE characterId IN (SELECT id FROM characters WHERE bookId = :bookId)")
  public suspend fun deleteForBook(bookId: BookId)
}
