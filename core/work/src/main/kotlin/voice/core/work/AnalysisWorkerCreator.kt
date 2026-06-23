package voice.core.work

import androidx.datastore.core.DataStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import voice.core.data.repo.AnalysisProgressRepository
import voice.core.data.repo.CharacterRepository
import voice.core.data.store.GeminiAnalysisModelStore
import voice.core.data.store.GeminiApiKeyStore
import voice.core.gemini.GeminiApi

public interface WorkerCreatorWithClass {
  public val workerClass: Class<out androidx.work.ListenableWorker>
  public val creator: WorkerCreator
}

@ContributesIntoSet(AppScope::class)
public class AnalysisWorkerCreator(
  private val characterRepository: CharacterRepository,
  private val analysisProgressRepository: AnalysisProgressRepository,
  private val geminiApi: GeminiApi,
  @GeminiApiKeyStore private val apiKeyStore: DataStore<String>,
  @GeminiAnalysisModelStore private val modelStore: DataStore<String>,
) : WorkerCreatorWithClass {
  override val workerClass: Class<out androidx.work.ListenableWorker> = AnalysisWorker::class.java
  override val creator: WorkerCreator = AnalysisWorker.Creator(
    characterRepository,
    analysisProgressRepository,
    geminiApi,
    apiKeyStore,
    modelStore,
  )
}
