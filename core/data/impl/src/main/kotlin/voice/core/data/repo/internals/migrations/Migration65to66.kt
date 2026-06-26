package voice.core.data.repo.internals.migrations

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.binding

@ContributesIntoSet(
  scope = AppScope::class,
  binding = binding<Migration>(),
)
public class Migration65to66 : IncrementalMigration(65) {
  override fun migrate(db: SupportSQLiteDatabase) {
    db.execSQL(
      """
      CREATE TABLE IF NOT EXISTS `narration_pieces` (
        `id` TEXT NOT NULL,
        `bookId` TEXT NOT NULL,
        `index` INTEGER NOT NULL,
        `text` TEXT NOT NULL,
        `characterName` TEXT NOT NULL,
        `isNewChapter` INTEGER NOT NULL,
        `chapterTitle` TEXT,
        PRIMARY KEY(`id`)
      )
      """.trimIndent()
    )
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_narration_pieces_bookId` ON `narration_pieces` (`bookId`)")
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_narration_pieces_bookId_index` ON `narration_pieces` (`bookId`, `index`)")
  }
}
