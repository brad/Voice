package voice.core.data.repo

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import voice.core.data.BookId
import voice.core.data.AnalysisProgress
import voice.core.data.repo.internals.dao.AnalysisProgressDao

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
public class AnalysisProgressRepositoryImpl(private val dao: AnalysisProgressDao) : AnalysisProgressRepository {
  override suspend fun progressForBook(bookId: BookId): AnalysisProgress? = dao.progressForBook(bookId)

  override fun flowProgressForBook(bookId: BookId): Flow<AnalysisProgress?> = dao.flowProgressForBook(bookId)

  override suspend fun insert(progress: AnalysisProgress): Unit = dao.insert(progress)

  override suspend fun deleteForBook(bookId: BookId): Unit = dao.deleteForBook(bookId)
}
