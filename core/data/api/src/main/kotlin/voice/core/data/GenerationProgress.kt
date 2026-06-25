package voice.core.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "generation_progress")
public data class GenerationProgress(
  @PrimaryKey
  val bookId: BookId,
  val status: GenerationStatus,
  val lastUpdated: Instant,
  val title: String? = null,
  val author: String? = null,
  val errorMessage: String? = null,
  val povType: String? = null,
  val povCharacterName: String? = null,
)
