package voice.core.work

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import voice.core.common.AppInfoProvider
import voice.core.data.AnalysisProgress
import voice.core.data.BookId
import voice.core.data.Character
import voice.core.data.GenerationProgress
import voice.core.data.GenerationStatus
import voice.core.data.NarrationPiece
import voice.core.data.PovType
import voice.core.data.VoiceMapping
import voice.core.data.WordPronunciation
import voice.core.data.repo.AnalysisProgressRepository
import voice.core.data.repo.CharacterRepository
import voice.core.data.repo.GenerationRepository
import voice.core.data.repo.NarrationPieceRepository
import voice.core.data.repo.VoiceMappingRepository
import voice.core.data.repo.WordPronunciationRepository
import voice.core.epub.EpubExtractor
import voice.core.gemini.Content
import voice.core.gemini.GeminiAnalysisPrompts
import voice.core.gemini.GeminiApi
import voice.core.gemini.GeminiClient
import voice.core.gemini.GenerateContentRequest
import voice.core.gemini.GenerationConfig
import voice.core.gemini.Part
import voice.core.logging.api.Logger
import java.time.Instant
import kotlin.uuid.Uuid

public class AnalysisWorker(
  context: Context,
  params: WorkerParameters,
  private val characterRepository: CharacterRepository,
  private val analysisProgressRepository: AnalysisProgressRepository,
  private val generationRepository: GenerationRepository,
  private val voiceMappingRepository: VoiceMappingRepository,
  private val wordPronunciationRepository: WordPronunciationRepository,
  private val narrationPieceRepository: NarrationPieceRepository,
  private val geminiApi: GeminiApi,
  private val apiKeyStore: DataStore<String>,
  private val modelStore: DataStore<String>,
  private val appInfoProvider: AppInfoProvider,
) : CoroutineWorker(context, params) {

  override suspend fun doWork(): Result {
    val bookIdString = inputData.getString(KEY_BOOK_ID) ?: return Result.failure()
    val bookId = BookId(bookIdString)

    var currentTitle: String? = null
    var currentAuthor: String? = null

    try {
      val apiKey = apiKeyStore.data.first()
      if (apiKey.isBlank()) {
        Logger.e("Gemini API key is missing")
        updateStatus(bookId, GenerationStatus.FAILED, "Gemini API key is missing")
        return Result.failure()
      }

      val model = modelStore.data.first().ifBlank { "gemini-3.1-flash-lite" }
      val client = GeminiClient(geminiApi, apiKey)

      val inputStream = try {
        applicationContext.contentResolver.openInputStream(bookId.toUri())
      } catch (e: Exception) {
        null
      } ?: run {
        generationRepository.insert(
          GenerationProgress(
            bookId = bookId,
            status = GenerationStatus.FAILED,
            lastUpdated = Instant.now(),
          ),
        )
        return Result.failure()
      }

      val epubData = try {
        EpubExtractor().extract(inputStream)
      } catch (e: Exception) {
        generationRepository.insert(
          GenerationProgress(
            bookId = bookId,
            status = GenerationStatus.FAILED,
            lastUpdated = Instant.now(),
          ),
        )
        return Result.failure()
      }

      currentTitle = epubData.title
      currentAuthor = epubData.author

      generationRepository.insert(
        GenerationProgress(
          bookId = bookId,
          status = GenerationStatus.ANALYZING,
          lastUpdated = Instant.now(),
          title = epubData.title,
          author = epubData.author,
        ),
      )
      val fullText = epubData.chapters.joinToString("\n\n") { it.content }

      val chunkSize = 16000
      val chunks = fullText.chunked(chunkSize)
      val totalChunks = chunks.size

      val progress = analysisProgressRepository.progressForBook(bookId)
      val startChunkIndex = progress?.currentChunkIndex ?: 0

      if (startChunkIndex == 0) {
        narrationPieceRepository.deleteForBook(bookId)
      }

      var currentCharacters = characterRepository.charactersForBook(bookId)

      for (i in startChunkIndex until totalChunks) {
        val chunk = chunks[i]
        val knownCharactersJson = Json.encodeToString(
          currentCharacters.map {
            SerializableCharacter(it.name, it.gender, it.age, it.energy, it.personality)
          },
        )

        val prompt = GeminiAnalysisPrompts.INCREMENTAL_CHARACTER_EXTRACTION_PROMPT.format(
          currentTitle,
          currentAuthor ?: "Unknown",
          knownCharactersJson,
          chunk,
        )

        val request = GenerateContentRequest(
          contents = listOf(Content(parts = listOf(Part(text = prompt)))),
          generationConfig = GenerationConfig(
            responseMimeType = "application/json",
            responseSchema = GeminiAnalysisPrompts.CHARACTER_EXTRACTION_SCHEMA,
          ),
        )

        val response = client.generateContent(model, request)
        val responseText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
          ?: throw Exception("Empty response from Gemini")

        val extracted = Json.decodeFromString<ExtractedData>(responseText)

        characterRepository.deleteForBook(bookId)
        val newCharacters = extracted.characters.map {
          Character(
            id = Uuid.random(),
            bookId = bookId,
            name = it.name,
            gender = it.gender,
            age = it.age,
            energy = it.energy,
            personality = it.personality,
          )
        }.toMutableList()

        // Ensure Narrator is present
        if (newCharacters.none { it.name.lowercase() == "narrator" }) {
          newCharacters.add(
            Character(
              id = Uuid.random(),
              bookId = bookId,
              name = "Narrator",
              gender = "Neutral",
              age = "Adult",
              energy = "Medium",
              personality = "Objective",
            ),
          )
        }

        characterRepository.insertAll(newCharacters)
        currentCharacters = newCharacters

        // Update POV
        generationRepository.updatePov(
          bookId = bookId,
          povType = extracted.povType,
          povCharacterName = extracted.povCharacterName,
        )

        // Extract and insert pronunciations (they are incremental for this chunk)
        val newPronunciations = extracted.pronunciations.map {
          WordPronunciation(
            id = Uuid.random(),
            bookId = bookId,
            word = it.word,
            phonetic = it.phonetic,
          )
        }
        wordPronunciationRepository.insertAll(newPronunciations)

        // Update voice mappings
        voiceMappingRepository.deleteForBook(bookId)
        val newMappings = mutableListOf<VoiceMapping>()
        for (char in newCharacters) {
          val mapping = VoiceMapper.mapToVoice(char, extracted.povType, extracted.povCharacterName, newMappings, newCharacters)
          newMappings.add(mapping)
        }
        voiceMappingRepository.insertAll(newMappings)

        // Save narration pieces
        val existingPiecesCount = narrationPieceRepository.getForBook(bookId).size
        val newNarrationPieces = extracted.narrationPieces.mapIndexed { index, piece ->
          NarrationPiece(
            id = Uuid.random(),
            bookId = bookId,
            index = existingPiecesCount + index,
            text = piece.text,
            characterName = piece.characterName,
            isNewChapter = piece.isNewChapter,
            chapterTitle = piece.chapterTitle,
          )
        }
        narrationPieceRepository.insertAll(newNarrationPieces)

        analysisProgressRepository.insert(
          AnalysisProgress(
            bookId = bookId,
            currentChunkIndex = i + 1,
            totalChunks = totalChunks,
            lastUpdated = Instant.now(),
          ),
        )
      }

      updateStatus(bookId, GenerationStatus.ANALYZED)
      return Result.success()
    } catch (e: Exception) {
      Logger.e(e, "Error during character extraction")
      if (runAttemptCount >= 3) {
        val report = ErrorReportGenerator.generate(
          throwable = e,
          appInfoProvider = appInfoProvider,
          bookTitle = currentTitle,
          bookAuthor = currentAuthor,
          step = "Character Analysis",
        )
        updateStatus(bookId, GenerationStatus.FAILED, report)
        return Result.failure()
      }
      return Result.retry()
    }
  }

  private suspend fun updateStatus(
    bookId: BookId,
    status: GenerationStatus,
    errorMessage: String? = null,
  ) {
    generationRepository.updateStatus(
      bookId = bookId,
      status = status,
      lastUpdated = Instant.now(),
      errorMessage = errorMessage,
    )
  }

  @Serializable
  internal data class SerializableCharacter(
    val name: String,
    val gender: String? = null,
    val age: String? = null,
    val energy: String? = null,
    val personality: String? = null,
  )

  @Serializable
  internal data class SerializablePronunciation(
    val word: String,
    val phonetic: String,
  )

  @Serializable
  internal data class SerializableNarrationPiece(
    val text: String,
    val characterName: String,
    val isNewChapter: Boolean,
    val chapterTitle: String? = null,
  )

  @Serializable
  internal data class ExtractedData(
    val characters: List<SerializableCharacter>,
    val pronunciations: List<SerializablePronunciation> = emptyList(),
    val povType: PovType,
    val povCharacterName: String? = null,
    val narrationPieces: List<SerializableNarrationPiece> = emptyList(),
  )

  public companion object {
    public const val KEY_BOOK_ID: String = "book_id"
  }

  public class Creator(
    private val characterRepository: CharacterRepository,
    private val analysisProgressRepository: AnalysisProgressRepository,
    private val generationRepository: GenerationRepository,
    private val voiceMappingRepository: VoiceMappingRepository,
    private val wordPronunciationRepository: WordPronunciationRepository,
    private val narrationPieceRepository: NarrationPieceRepository,
    private val geminiApi: GeminiApi,
    private val apiKeyStore: DataStore<String>,
    private val modelStore: DataStore<String>,
    private val appInfoProvider: AppInfoProvider,
  ) : WorkerCreator {
    override fun create(
      context: Context,
      parameters: WorkerParameters,
    ): ListenableWorker {
      return AnalysisWorker(
        context,
        parameters,
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
  }
}
