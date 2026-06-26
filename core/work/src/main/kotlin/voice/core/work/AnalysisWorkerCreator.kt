package voice.core.work

import androidx.datastore.core.DataStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import voice.core.common.AppInfoProvider
import voice.core.data.repo.AnalysisProgressRepository
import voice.core.data.repo.CharacterRepository
import voice.core.data.repo.GenerationRepository
import voice.core.data.repo.NarrationPieceRepository
import voice.core.data.repo.VoiceMappingRepository
import voice.core.data.repo.WordPronunciationRepository
import voice.core.data.store.GeminiAnalysisModelStore
import voice.core.data.store.GeminiApiKeyStore
import voice.core.gemini.GeminiApi

@ContributesIntoSet(AppScope::class)
public class AnalysisWorkerCreator(
  private val characterRepository: CharacterRepository,
  private val analysisProgressRepository: AnalysisProgressRepository,
  private val generationRepository: GenerationRepository,
  private val voiceMappingRepository: VoiceMappingRepository,
  private val wordPronunciationRepository: WordPronunciationRepository,
  private val narrationPieceRepository: NarrationPieceRepository,
  private val geminiApi: GeminiApi,
  @GeminiApiKeyStore private val apiKeyStore: DataStore<String>,
  @GeminiAnalysisModelStore private val modelStore: DataStore<String>,
  private val appInfoProvider: AppInfoProvider,
) : WorkerCreatorWithClass {
  override val workerClass: Class<out androidx.work.ListenableWorker> = AnalysisWorker::class.java
  override val creator: WorkerCreator = AnalysisWorker.Creator(
    characterRepository,
    analysisProgressRepository,
    generationRepository,
    voiceMappingRepository,
    wordPronunciationRepository,
    narrationPieceRepository,
    geminiApi,
    apiKeyStore,
    modelStore,
    appInfoProvider,
  )
}
