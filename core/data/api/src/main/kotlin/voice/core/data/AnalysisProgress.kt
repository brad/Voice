package voice.core.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "analysis_progress")
public data class AnalysisProgress(
  @PrimaryKey
  val bookId: BookId,
  val currentChunkIndex: Int,
  val totalChunks: Int,
  val lastUpdated: Instant,
)
