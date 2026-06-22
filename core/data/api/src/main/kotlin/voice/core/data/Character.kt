package voice.core.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.uuid.Uuid

@Entity(
  tableName = "characters",
  indices = [Index("bookId")],
)
public data class Character(
  @PrimaryKey
  val id: Uuid,
  val bookId: BookId,
  val name: String,
  val gender: String?,
  val age: String?,
  val energy: String?,
  val personality: String?,
)
