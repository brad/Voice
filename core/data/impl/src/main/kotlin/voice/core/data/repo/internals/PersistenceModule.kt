package voice.core.data.repo.internals

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import voice.core.data.repo.internals.dao.BookContentDao
import voice.core.data.repo.internals.dao.BookmarkDao
import voice.core.data.repo.internals.dao.ChapterDao
import voice.core.data.repo.internals.dao.CharacterDao
import voice.core.data.repo.internals.dao.GenerationProgressDao
import voice.core.data.repo.internals.dao.RecentBookSearchDao
import voice.core.data.repo.internals.dao.VoiceMappingDao
import voice.core.data.repo.internals.dao.WordPronunciationDao

@ContributesTo(AppScope::class)
public interface PersistenceModule {

  @Provides
  private fun chapterDao(appDb: AppDb): ChapterDao = appDb.chapterDao()

  @Provides
  private fun bookContentDao(appDb: AppDb): BookContentDao = appDb.bookContentDao()

  @Provides
  private fun bookmarkDao(appDb: AppDb): BookmarkDao = appDb.bookmarkDao()

  @Provides
  private fun recentBookSearchDao(appDb: AppDb): RecentBookSearchDao = appDb.recentBookSearchDao()

  @Provides
  private fun characterDao(appDb: AppDb): CharacterDao = appDb.characterDao()

  @Provides
  private fun voiceMappingDao(appDb: AppDb): VoiceMappingDao = appDb.voiceMappingDao()

  @Provides
  private fun wordPronunciationDao(appDb: AppDb): WordPronunciationDao = appDb.wordPronunciationDao()

  @Provides
  private fun generationProgressDao(appDb: AppDb): GenerationProgressDao = appDb.generationProgressDao()

  @Provides
  @SingleIn(AppScope::class)
  private fun appDb(
    context: Context,
    migrations: Set<@JvmSuppressWildcards Migration>,
  ): AppDb {
    return Room.databaseBuilder(context, AppDb::class.java, AppDb.DATABASE_NAME)
      .addMigrations(*migrations.toTypedArray())
      .build()
  }

  @Provides
  private fun bindRoomDatabase(appDb: AppDb): RoomDatabase = appDb
}
