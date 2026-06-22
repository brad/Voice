package voice.core.data.repo.internals.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import voice.core.data.VoiceMapping
import kotlin.uuid.Uuid

@Dao
public interface VoiceMappingDao {
  @Query("SELECT * FROM voice_mappings WHERE characterId = :characterId")
  public suspend fun mappingForCharacter(characterId: Uuid): VoiceMapping?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public suspend fun insert(mapping: VoiceMapping)

  @Delete
  public suspend fun delete(mapping: VoiceMapping)
}
