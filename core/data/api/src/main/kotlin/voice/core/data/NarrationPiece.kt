package voice.core.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.uuid.Uuid

@Entity(
  tableName = "narration_pieces",
  indices = [Index("bookId"), Index("bookId", "index")],
)
public data class NarrationPiece(
  @PrimaryKey
  val id: Uuid,
  val bookId: BookId,
  val index: Int,
  val text: String,
  val characterName: String,
  val isNewChapter: Boolean,
  val chapterTitle: String?,
)
