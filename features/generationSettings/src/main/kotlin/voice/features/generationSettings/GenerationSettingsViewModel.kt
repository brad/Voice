package voice.features.generationSettings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch
import voice.core.data.BookId
import voice.core.data.Character
import voice.core.data.GenerationStatus
import voice.core.data.VoiceMapping
import voice.core.data.repo.CharacterRepository
import voice.core.data.repo.GenerationRepository
import voice.core.data.repo.VoiceMappingRepository
import voice.core.work.AudiobookGenerationManager
import voice.navigation.Navigator

@Inject
public class GenerationSettingsViewModel(
  private val characterRepository: CharacterRepository,
  private val voiceMappingRepository: VoiceMappingRepository,
  private val generationRepository: GenerationRepository,
  private val generationManager: AudiobookGenerationManager,
  private val navigator: Navigator,
) {

  @Composable
  internal fun state(bookId: BookId): GenerationSettingsViewState {
    val characters by remember(bookId) {
      characterRepository.flowCharactersForBook(bookId)
    }.collectAsState(initial = emptyList())

    val voiceMappings by remember(bookId) {
      voiceMappingRepository.flowMappingsForBook(bookId)
    }.collectAsState(initial = emptyList())

    val generationProgress by remember(bookId) {
      generationRepository.flowProgressForBook(bookId)
    }.collectAsState(initial = null)

    val scope = rememberCoroutineScope()

    return GenerationSettingsViewState(
      characters = characters,
      voiceMappings = voiceMappings.associateBy { it.characterId },
      status = generationProgress?.status,
      onDiscardAndRestart = {
        scope.launch {
          generationManager.discardGeneration(bookId)
        }
      },
    )
  }

  public fun startGeneration(bookId: BookId) {
    generationManager.generateAudiobook(bookId)
    navigator.goBack()
  }

  public fun close() {
    navigator.goBack()
  }
}

internal data class GenerationSettingsViewState(
  val characters: List<Character>,
  val voiceMappings: Map<kotlin.uuid.Uuid, VoiceMapping>,
  val status: GenerationStatus?,
  val onDiscardAndRestart: () -> Unit,
)
