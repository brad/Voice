package voice.core.data.repo

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import voice.core.data.AudioGenerationProgress
import voice.core.data.BookId
import voice.core.data.repo.internals.dao.AudioGenerationProgressDao

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
public class AudioGenerationProgressRepositoryImpl(private val dao: AudioGenerationProgressDao) : AudioGenerationProgressRepository {
  override suspend fun progressForBook(bookId: BookId): AudioGenerationProgress? = dao.progressForBook(bookId)

  override fun flowProgressForBook(bookId: BookId): Flow<AudioGenerationProgress?> = dao.flowProgressForBook(bookId)

  override suspend fun insert(progress: AudioGenerationProgress): Unit = dao.insert(progress)

  override suspend fun deleteForBook(bookId: BookId): Unit = dao.deleteForBook(bookId)
}
