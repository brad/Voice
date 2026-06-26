package voice.core.data.repo.internals

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import voice.core.data.repo.internals.dao.AnalysisProgressDao
import voice.core.data.repo.internals.dao.AudioGenerationProgressDao
import voice.core.data.repo.internals.dao.BookContentDao
import voice.core.data.repo.internals.dao.BookmarkDao
import voice.core.data.repo.internals.dao.ChapterDao
import voice.core.data.repo.internals.dao.CharacterDao
import voice.core.data.repo.internals.dao.GenerationProgressDao
import voice.core.data.repo.internals.dao.NarrationPieceDao
import voice.core.data.repo.internals.dao.RecentBookSearchDao
import voice.core.data.repo.internals.dao.VoiceMappingDao
import voice.core.data.repo.internals.dao.WordPronunciationDao

@ContributesTo(AppScope::class)
public interface PersistenceModule {

  @Provides
  @SingleIn(AppScope::class)
  public fun provideAppDb(
    context: Context,
    migrations: Set<Migration>,
  ): AppDb {
    return Room.databaseBuilder(
      context,
      AppDb::class.java,
      AppDb.DATABASE_NAME,
    ).addMigrations(*migrations.toTypedArray()).build()
  }

  @Provides
  public fun provideChapterDao(db: AppDb): ChapterDao = db.chapterDao()

  @Provides
  public fun provideBookContentDao(db: AppDb): BookContentDao = db.bookContentDao()

  @Provides
  public fun provideBookmarkDao(db: AppDb): BookmarkDao = db.bookmarkDao()

  @Provides
  public fun provideRecentBookSearchDao(db: AppDb): RecentBookSearchDao = db.recentBookSearchDao()

  @Provides
  public fun provideCharacterDao(db: AppDb): CharacterDao = db.characterDao()

  @Provides
  public fun provideVoiceMappingDao(db: AppDb): VoiceMappingDao = db.voiceMappingDao()

  @Provides
  public fun provideWordPronunciationDao(db: AppDb): WordPronunciationDao = db.wordPronunciationDao()

  @Provides
  public fun provideGenerationProgressDao(db: AppDb): GenerationProgressDao = db.generationProgressDao()

  @Provides
  public fun provideAnalysisProgressDao(db: AppDb): AnalysisProgressDao = db.analysisProgressDao()

  @Provides
  public fun provideAudioGenerationProgressDao(db: AppDb): AudioGenerationProgressDao = db.audioGenerationProgressDao()

  @Provides
  public fun provideNarrationPieceDao(db: AppDb): NarrationPieceDao = db.narrationPieceDao()
}
