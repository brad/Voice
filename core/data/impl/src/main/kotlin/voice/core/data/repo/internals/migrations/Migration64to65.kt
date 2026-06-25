package voice.core.data.repo.internals.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.binding

@ContributesIntoSet(
  scope = AppScope::class,
  binding = binding<Migration>(),
)
public class Migration64to65 : IncrementalMigration(64) {

  override fun migrate(db: SupportSQLiteDatabase) {
    // 1. Create a new table with the correct schema
    db.execSQL(
      """
      CREATE TABLE `generation_progress_new` (
        `bookId` TEXT NOT NULL,
        `status` TEXT NOT NULL,
        `lastUpdated` TEXT NOT NULL,
        `title` TEXT,
        `author` TEXT,
        `errorMessage` TEXT,
        `povType` TEXT,
        `povCharacterName` TEXT,
        PRIMARY KEY(`bookId`)
      )
      """.trimIndent(),
    )

    // 2. Identify which columns exist in the old table to copy them safely
    val cursor = db.query("PRAGMA table_info(generation_progress)")
    val existingColumns = mutableListOf<String>()
    while (cursor.moveToNext()) {
      val columnName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
      existingColumns.add(columnName)
    }
    cursor.close()

    val columnsToCopy = listOf(
      "bookId",
      "status",
      "lastUpdated",
      "title",
      "author",
      "errorMessage",
      "povType",
      "povCharacterName",
    ).filter { it in existingColumns }

    if (columnsToCopy.isNotEmpty()) {
      val columnsCsv = columnsToCopy.joinToString(", ") { "`$it`" }
      db.execSQL(
        "INSERT INTO `generation_progress_new` ($columnsCsv) SELECT $columnsCsv FROM `generation_progress`",
      )
    }

    // 3. Drop the old table and rename the new one
    db.execSQL("DROP TABLE `generation_progress`")
    db.execSQL("ALTER TABLE `generation_progress_new` RENAME TO `generation_progress`")
  }
}
