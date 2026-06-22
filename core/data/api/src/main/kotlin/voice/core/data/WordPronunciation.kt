package voice.core.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.uuid.Uuid

@Entity(
  tableName = "word_pronunciations",
  indices = [Index("bookId")],
)
public data class WordPronunciation(
  @PrimaryKey
  val id: Uuid,
  val bookId: BookId,
  val word: String,
  val phonetic: String,
)
