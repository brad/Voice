package voice.core.data.repo

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import voice.core.data.BookId
import java.time.Instant
import voice.core.data.GenerationProgress
import voice.core.data.GenerationStatus
import voice.core.data.repo.internals.dao.GenerationProgressDao

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
public class GenerationRepositoryImpl(private val dao: GenerationProgressDao) : GenerationRepository {
  override suspend fun progressForBook(bookId: BookId): GenerationProgress? = dao.progressForBook(bookId)

  override fun flowProgressForBook(bookId: BookId): Flow<GenerationProgress?> = dao.flowProgressForBook(bookId)

  override suspend fun insert(progress: GenerationProgress): Unit = dao.insert(progress)

  override suspend fun deleteForBook(bookId: BookId): Unit = dao.deleteForBook(bookId)
  override suspend fun updateStatus(bookId: BookId, status: GenerationStatus, lastUpdated: Instant): Unit = dao.updateStatus(bookId, status, lastUpdated)
  override fun flowInProgressGenerations(): Flow<List<GenerationProgress>> = dao.flowInProgressGenerations(GenerationStatus.COMPLETED)
}
