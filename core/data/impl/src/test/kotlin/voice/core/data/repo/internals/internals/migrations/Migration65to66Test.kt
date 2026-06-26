package voice.core.data.repo.internals.internals.migrations

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import voice.core.data.repo.internals.getString
import voice.core.data.repo.internals.migrations.Migration65to66
import kotlin.test.Test
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class Migration65to66Test {

  private lateinit var db: SupportSQLiteDatabase
  private lateinit var helper: SupportSQLiteOpenHelper

  @Before
  fun setUp() {
    val config = SupportSQLiteOpenHelper.Configuration
      .builder(getApplicationContext())
      .callback(
        object : SupportSQLiteOpenHelper.Callback(65) {
          override fun onCreate(db: SupportSQLiteDatabase) {
            // Version 65 doesn't have narration_pieces
          }

          override fun onUpgrade(
            db: SupportSQLiteDatabase,
            oldVersion: Int,
            newVersion: Int,
          ) {}
        },
      )
      .build()
    helper = FrameworkSQLiteOpenHelperFactory().create(config)
    db = helper.writableDatabase
  }

  @After
  fun tearDown() {
    helper.close()
  }

  @Test
  fun migrationCreatesTableAndIndices() {
    // Run migration
    Migration65to66().migrate(db)

    // Verify table exists and has correct columns
    val cursor = db.query("PRAGMA table_info(narration_pieces)")
    val columns = mutableListOf<String>()
    while (cursor.moveToNext()) {
      columns.add(cursor.getString(cursor.getColumnIndex("name")))
    }
    cursor.close()

    assertTrue(columns.contains("id"))
    assertTrue(columns.contains("bookId"))
    assertTrue(columns.contains("index"))
    assertTrue(columns.contains("text"))
    assertTrue(columns.contains("characterName"))
    assertTrue(columns.contains("isNewChapter"))
    assertTrue(columns.contains("chapterTitle"))

    // Verify indices exist
    val indexCursor = db.query("PRAGMA index_list(narration_pieces)")
    val indices = mutableListOf<String>()
    while (indexCursor.moveToNext()) {
      indices.add(indexCursor.getString(indexCursor.getColumnIndex("name")))
    }
    indexCursor.close()

    assertTrue(indices.contains("index_narration_pieces_bookId"))
    assertTrue(indices.contains("index_narration_pieces_bookId_index"))
  }
}
