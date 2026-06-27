package voice.core.data.repo.internals.internals

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.runner.RunWith
import voice.core.data.repo.internals.AppDb
import voice.core.data.repo.internals.allMigrations
import voice.core.data.repo.internals.getStringOrNull
import voice.core.data.repo.internals.mapRows
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class Migration66to67Test {

  @Rule
  @JvmField
  val helper = MigrationTestHelper(
    InstrumentationRegistry.getInstrumentation(),
    AppDb::class.java,
  )

  @Test
  fun migrate66to67() {
    val dbName = "testDb"
    val db = helper.createDatabase(dbName, 66)

    // Insert data into generation_progress (version 66 schema)
    db.execSQL(
      "INSERT INTO generation_progress (bookId, status, lastUpdated, title, author, errorMessage, povType, povCharacterName) " +
        "VALUES ('book1', 'ANALYZING', '2023-01-01T00:00:00Z', 'Title', 'Author', NULL, 'THIRD_PERSON_LIMITED', 'Char')",
    )
    db.close()

    val migratedDb = helper.runMigrationsAndValidate(
      dbName,
      67,
      true,
      *allMigrations(),
    )

    val progress = migratedDb.query("SELECT * FROM generation_progress WHERE bookId = 'book1'").mapRows {
      getStringOrNull("retryAfter")
    }

    assertEquals(1, progress.size)
    assertEquals(null, progress[0])

    // Verify we can update retryAfter
    migratedDb.execSQL("UPDATE generation_progress SET retryAfter = '2023-01-01T00:00:10Z' WHERE bookId = 'book1'")
    val updatedProgress = migratedDb.query("SELECT * FROM generation_progress WHERE bookId = 'book1'").mapRows {
      getStringOrNull("retryAfter")
    }
    assertEquals("2023-01-01T00:00:10Z", updatedProgress[0])
  }
}
