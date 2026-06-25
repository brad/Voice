package voice.core.data.repo.internals.internals.migrations

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import voice.core.data.repo.internals.getString
import voice.core.data.repo.internals.mapRows
import voice.core.data.repo.internals.migrations.Migration64to65
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class Migration64to65Test {

  private lateinit var db: SupportSQLiteDatabase
  private lateinit var helper: SupportSQLiteOpenHelper

  @Before
  fun setUp() {
    val config = SupportSQLiteOpenHelper.Configuration
      .builder(getApplicationContext())
      .callback(
        object : SupportSQLiteOpenHelper.Callback(64) {
          override fun onCreate(db: SupportSQLiteDatabase) {
            // Create table with "broken" schema (missing errorMessage and having default NULLs)
            db.execSQL(
              """
              CREATE TABLE `generation_progress` (
                `bookId` TEXT NOT NULL,
                `status` TEXT NOT NULL,
                `lastUpdated` TEXT NOT NULL,
                `title` TEXT DEFAULT NULL,
                `author` TEXT DEFAULT NULL,
                PRIMARY KEY(`bookId`)
              )
              """.trimIndent(),
            )
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
  fun migrationFixesSchema() {
    // Insert some data into the broken table
    val cv = ContentValues().apply {
      put("bookId", "book-1")
      put("status", "ANALYZING")
      put("lastUpdated", "2026-06-25T09:00:00Z")
      put("title", "Test Title")
    }
    db.insert("generation_progress", SQLiteDatabase.CONFLICT_FAIL, cv)

    // Run migration
    Migration64to65().migrate(db)

    // Verify columns exist
    val cursor = db.query("PRAGMA table_info(generation_progress)")
    val columns = mutableListOf<String>()
    val defaults = mutableMapOf<String, String?>()
    while (cursor.moveToNext()) {
      val name = cursor.getString(cursor.getColumnIndex("name"))
      columns.add(name)
      val dfltIdx = cursor.getColumnIndex("dflt_value")
      defaults[name] = if (cursor.isNull(dfltIdx)) null else cursor.getString(dfltIdx)
    }
    cursor.close()

    assertTrue(columns.contains("errorMessage"), "errorMessage column should exist")
    assertTrue(columns.contains("povType"), "povType column should exist")
    assertTrue(columns.contains("povCharacterName"), "povCharacterName column should exist")

    // Verify defaults are removed (should be null in PRAGMA table_info when no DEFAULT clause is present)
    assertEquals(null, defaults["author"], "author should not have a default value")
    assertEquals(null, defaults["title"], "title should not have a default value")

    // Verify data is preserved
    val data = db.query("SELECT * FROM generation_progress").mapRows {
      getString("bookId") to getString("title")
    }
    assertEquals(listOf("book-1" to "Test Title"), data)
  }
}
