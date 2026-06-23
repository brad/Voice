package voice.core.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "audio_generation_progress")
public data class AudioGenerationProgress(
  @PrimaryKey
  val bookId: BookId,
  val chapterIndex: Int,
  val chunkIndex: Int,
  val totalChunks: Int,
  val lastUpdated: Instant,
)
