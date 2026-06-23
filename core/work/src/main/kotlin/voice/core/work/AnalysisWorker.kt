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
import voice.core.data.AnalysisProgress
import voice.core.data.BookId
import voice.core.data.Character
import voice.core.data.repo.AnalysisProgressRepository
import voice.core.data.repo.CharacterRepository
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
  private val geminiApi: GeminiApi,
  private val apiKeyStore: DataStore<String>,
  private val modelStore: DataStore<String>,
) : CoroutineWorker(context, params) {

  override suspend fun doWork(): Result {
    val bookIdString = inputData.getString(KEY_BOOK_ID) ?: return Result.failure()
    val bookId = BookId(bookIdString)

    val apiKey = apiKeyStore.data.first()
    if (apiKey.isBlank()) {
      Logger.e("Gemini API key is missing")
      return Result.failure()
    }

    val model = modelStore.data.first().ifBlank { "gemini-1.5-flash" }
    val client = GeminiClient(geminiApi, apiKey)

    val inputStream = try {
      applicationContext.contentResolver.openInputStream(bookId.toUri())
    } catch (e: Exception) {
      null
    } ?: return Result.failure()

    val epubData = EpubExtractor().extract(inputStream)
    val fullText = epubData.chapters.joinToString("\n\n") { it.content }

    val chunkSize = 32000
    val chunks = fullText.chunked(chunkSize)
    val totalChunks = chunks.size

    val progress = analysisProgressRepository.progressForBook(bookId)
    val startChunkIndex = progress?.currentChunkIndex ?: 0

    var currentCharacters = characterRepository.charactersForBook(bookId)

    for (i in startChunkIndex until totalChunks) {
      val chunk = chunks[i]
      val knownCharactersJson = Json.encodeToString(
        currentCharacters.map {
          SerializableCharacter(it.name, it.gender, it.age, it.energy, it.personality)
        },
      )

      val prompt = GeminiAnalysisPrompts.INCREMENTAL_CHARACTER_EXTRACTION_PROMPT.format(
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

      try {
        val response = client.generateContent(model, request)
        val responseText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
          ?: throw Exception("Empty response from Gemini")

        val extracted = Json.decodeFromString<ExtractedCharacters>(responseText)

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
        }
        characterRepository.insertAll(newCharacters)
        currentCharacters = newCharacters

        analysisProgressRepository.insert(
          AnalysisProgress(
            bookId = bookId,
            currentChunkIndex = i + 1,
            totalChunks = totalChunks,
            lastUpdated = Instant.now(),
          ),
        )
      } catch (e: Exception) {
        Logger.e(e, "Error during character extraction for chunk $i")
        return Result.retry()
      }
    }

    return Result.success()
  }

  @Serializable
  private data class SerializableCharacter(
    val name: String,
    val gender: String?,
    val age: String?,
    val energy: String?,
    val personality: String?,
  )

  @Serializable
  private data class ExtractedCharacters(val characters: List<SerializableCharacter>)

  public companion object {
    public const val KEY_BOOK_ID: String = "book_id"
  }

  public class Creator(
    private val characterRepository: CharacterRepository,
    private val analysisProgressRepository: AnalysisProgressRepository,
    private val geminiApi: GeminiApi,
    private val apiKeyStore: DataStore<String>,
    private val modelStore: DataStore<String>,
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
        geminiApi,
        apiKeyStore,
        modelStore,
      )
    }
  }
}
