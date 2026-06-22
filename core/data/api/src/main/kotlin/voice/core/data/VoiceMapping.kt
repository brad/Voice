package voice.core.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.uuid.Uuid

@Entity(
  tableName = "voice_mappings",
  indices = [Index("characterId")],
)
public data class VoiceMapping(
  @PrimaryKey
  val id: Uuid,
  val characterId: Uuid,
  val voiceName: String,
  val speed: Float,
  val pitch: Float,
  val energy: Float,
)
