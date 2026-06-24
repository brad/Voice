package voice.core.work

import androidx.datastore.core.DataStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import voice.core.data.repo.AudioGenerationProgressRepository
import voice.core.data.repo.BookContentRepo
import voice.core.data.repo.CharacterRepository
import voice.core.data.repo.GenerationRepository
import voice.core.data.repo.VoiceMappingRepository
import voice.core.data.repo.WordPronunciationRepository
import voice.core.data.store.GeminiApiKeyStore
import voice.core.data.store.GeminiGenerationModelStore
import voice.core.gemini.GeminiApi
import voice.core.scanner.MediaScanTrigger

@ContributesIntoSet(AppScope::class)
public class GenerationWorkerCreator(
  private val characterRepository: CharacterRepository,
  private val voiceMappingRepository: VoiceMappingRepository,
  private val wordPronunciationRepository: WordPronunciationRepository,
  private val audioGenerationProgressRepository: AudioGenerationProgressRepository,
  private val generationRepository: GenerationRepository,
  private val bookContentRepo: BookContentRepo,
  private val geminiApi: GeminiApi,
  @GeminiApiKeyStore private val apiKeyStore: DataStore<String>,
  @GeminiGenerationModelStore private val modelStore: DataStore<String>,
  private val mediaScanTrigger: MediaScanTrigger,
) : WorkerCreatorWithClass {
  override val workerClass: Class<out androidx.work.ListenableWorker> = GenerationWorker::class.java
  override val creator: WorkerCreator = GenerationWorker.Creator(
    characterRepository,
    voiceMappingRepository,
    wordPronunciationRepository,
    audioGenerationProgressRepository,
    generationRepository,
    bookContentRepo,
    geminiApi,
    apiKeyStore,
    modelStore,
    mediaScanTrigger,
  )
}
