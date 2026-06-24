package voice.core.work

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import voice.core.data.AudioGenerationProgress
import voice.core.data.BookId
import voice.core.data.GenerationProgress
import voice.core.data.GenerationStatus
import voice.core.data.repo.AudioGenerationProgressRepository
import voice.core.data.repo.BookContentRepo
import voice.core.data.repo.CharacterRepository
import voice.core.data.repo.GenerationRepository
import voice.core.data.repo.VoiceMappingRepository
import voice.core.data.repo.WordPronunciationRepository
import voice.core.epub.EpubExtractor
import voice.core.gemini.Content
import voice.core.gemini.GeminiApi
import voice.core.gemini.GeminiClient
import voice.core.gemini.GenerateContentRequest
import voice.core.gemini.GenerationConfig
import voice.core.gemini.MultiSpeakerVoiceConfig
import voice.core.gemini.Part
import voice.core.gemini.PrebuiltVoiceConfig
import voice.core.gemini.SpeakerVoiceConfig
import voice.core.gemini.SpeechConfig
import voice.core.gemini.VoiceConfig
import voice.core.logging.api.Logger
import voice.core.scanner.MediaScanTrigger
import java.io.File
import java.io.FileOutputStream
import java.time.Instant

public class GenerationWorker(
  context: Context,
  params: WorkerParameters,
  private val characterRepository: CharacterRepository,
  private val voiceMappingRepository: VoiceMappingRepository,
  private val wordPronunciationRepository: WordPronunciationRepository,
  private val audioGenerationProgressRepository: AudioGenerationProgressRepository,
  private val generationRepository: GenerationRepository,
  private val bookContentRepo: BookContentRepo,
  private val geminiApi: GeminiApi,
  private val apiKeyStore: DataStore<String>,
  private val modelStore: DataStore<String>,
  private val mediaScanTrigger: MediaScanTrigger,
) : CoroutineWorker(context, params) {

  override suspend fun doWork(): Result {
    val bookIdString = inputData.getString(KEY_BOOK_ID) ?: return Result.failure()
    val bookId = BookId(bookIdString)

    val apiKey = apiKeyStore.data.first()
    if (apiKey.isBlank()) {
      Logger.e("Gemini API key is missing")
      return Result.failure()
    }

    updateStatus(bookId, GenerationStatus.GENERATING)

    val model = modelStore.data.first().ifBlank { "gemini-3.1-flash-tts-preview" }
    val client = GeminiClient(geminiApi, apiKey)

    val inputStream = try {
      applicationContext.contentResolver.openInputStream(bookId.toUri())
    } catch (e: Exception) {
      null
    } ?: run {
      updateStatus(bookId, GenerationStatus.FAILED)
      return Result.failure()
    }

    val epubData = try {
      EpubExtractor().extract(inputStream)
    } catch (e: Exception) {
      updateStatus(bookId, GenerationStatus.FAILED)
      return Result.failure()
    }

    val characters = characterRepository.charactersForBook(bookId)
    val mappings = voiceMappingRepository.mappingsForBook(bookId)
    val pronunciations = wordPronunciationRepository.pronunciationsForBook(bookId)

    val progress = audioGenerationProgressRepository.progressForBook(bookId)
    val startChapterIndex = progress?.chapterIndex ?: 0
    val startChunkIndex = progress?.chunkIndex ?: 0

    val outputDir = File(applicationContext.filesDir, "audiobooks/${bookId.value}")
    outputDir.mkdirs()

    for (chapterIdx in startChapterIndex until epubData.chapters.size) {
      val chapter = epubData.chapters[chapterIdx]
      val chapterText = chapter.content

      // Simple chunking by paragraph or sentence to stay within token limits
      val paragraphs = chapterText.split("\n\n").filter { it.isNotBlank() }
      val totalChunks = paragraphs.size

      val currentChunkStart = if (chapterIdx == startChapterIndex) startChunkIndex else 0

      val chapterChunks = mutableListOf<File>()

      for (chunkIdx in currentChunkStart until totalChunks) {
        val chunkText = paragraphs[chunkIdx]

        // Prepare multi-speaker prompt
        // In a real implementation, we would use Gemini to label the speakers in the text
        // For now, we'll use a simplified approach: wrap text in speaker labels
        // and include mappings in the config.
        val characterInstructions = characters.joinToString("\n") { char ->
          val mapping = mappings.find { it.characterId == char.id }
          val tuning = if (mapping != null) {
            " (Speed: ${mapping.speed}, Pitch: ${mapping.pitch}, Energy: ${mapping.energy})"
          } else {
            ""
          }
          "- ${char.name}: ${char.personality ?: "Narrator"}$tuning"
        }

        val pronunciationInstructions = if (pronunciations.isNotEmpty()) {
          "\n\nPronunciation Guide:\n" + pronunciations.joinToString("\n") { "${it.word} -> ${it.phonetic}" }
        } else {
          ""
        }

        val speakerPrompt = """
          Perform a multi-speaker TTS generation for the following book excerpt.
          Identify the speakers (including the Narrator) and assign them the appropriate voices from the configuration.
          Character Profiles and Voice Tuning:
          $characterInstructions$pronunciationInstructions

          Excerpt:
          $chunkText
        """.trimIndent()

        val speakerVoiceConfigs = mappings.map { mapping ->
          val character = characters.find { it.id == mapping.characterId }
          SpeakerVoiceConfig(
            speaker = character?.name ?: "Narrator",
            voiceConfig = VoiceConfig(
              prebuiltVoiceConfig = PrebuiltVoiceConfig(voiceName = mapping.voiceName),
            ),
          )
        }

        val request = GenerateContentRequest(
          contents = listOf(Content(parts = listOf(Part(text = speakerPrompt)))),
          generationConfig = GenerationConfig(
            responseModalities = listOf("AUDIO"),
            speechConfig = SpeechConfig(
              multiSpeakerVoiceConfig = MultiSpeakerVoiceConfig(speakerVoiceConfigs),
            ),
          ),
        )

        try {
          val response = client.generateContent(model, request)
          val audioData = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.inlineData?.data
            ?: throw Exception("No audio data in response")

          val chunkFile = File(outputDir, "ch_${chapterIdx}_chunk_$chunkIdx.raw")
          FileOutputStream(chunkFile).use { fos ->
            fos.write(Base64.decode(audioData, Base64.DEFAULT))
          }
          chapterChunks.add(chunkFile)

          audioGenerationProgressRepository.insert(
            AudioGenerationProgress(
              bookId = bookId,
              chapterIndex = chapterIdx,
              chunkIndex = chunkIdx + 1,
              totalChunks = totalChunks,
              lastUpdated = Instant.now(),
            ),
          )
        } catch (e: Exception) {
          Logger.e(e, "Error generating audio for chapter $chapterIdx chunk $chunkIdx")
          return Result.retry()
        }
      }

      // Merge chunks into chapter WAV
      if (chapterChunks.isNotEmpty()) {
        val chapterFile = File(outputDir, "chapter_$chapterIdx.wav")
        mergePcmFilesToWav(chapterChunks, chapterFile)
        chapterChunks.forEach { it.delete() }
      }
    }

    updateStatus(bookId, GenerationStatus.COMPLETED)
    mediaScanTrigger.scan()

    // Improve metadata after scan
    val internalId = BookId(outputDir.toURI().toString())
    val generatedContent = bookContentRepo.get(internalId)
    if (generatedContent != null) {
      bookContentRepo.put(generatedContent.copy(name = epubData.title))
    }

    return Result.success()
  }

  private fun mergePcmFilesToWav(
    pcmFiles: List<File>,
    outputFile: File,
  ) {
    // Basic WAV header for 24kHz, 16-bit, mono PCM (common Gemini TTS output)
    val sampleRate = 24000
    val channels = 1
    val bitDepth = 16

    var totalAudioLen = 0L
    pcmFiles.forEach { totalAudioLen += it.length() }

    val totalDataLen = totalAudioLen + 36
    val byteRate = (sampleRate * channels * bitDepth / 8).toLong()

    FileOutputStream(outputFile).use { out ->
      // RIFF header
      out.write("RIFF".toByteArray())
      out.write(intToByteArray(totalDataLen.toInt()))
      out.write("WAVE".toByteArray())

      // fmt sub-chunk
      out.write("fmt ".toByteArray())
      out.write(intToByteArray(16)) // sub-chunk size
      out.write(shortToByteArray(1)) // audio format (PCM)
      out.write(shortToByteArray(channels.toShort()))
      out.write(intToByteArray(sampleRate))
      out.write(intToByteArray(byteRate.toInt()))
      out.write(shortToByteArray((channels * bitDepth / 8).toShort())) // block align
      out.write(shortToByteArray(bitDepth.toShort()))

      // data sub-chunk
      out.write("data".toByteArray())
      out.write(intToByteArray(totalAudioLen.toInt()))

      pcmFiles.forEach { file ->
        file.inputStream().use { input ->
          input.copyTo(out)
        }
      }
    }
  }

  private fun intToByteArray(value: Int): ByteArray = byteArrayOf(
    (value and 0xff).toByte(),
    (value shr 8 and 0xff).toByte(),
    (value shr 16 and 0xff).toByte(),
    (value shr 24 and 0xff).toByte(),
  )

  private fun shortToByteArray(value: Short): ByteArray = byteArrayOf(
    (value.toInt() and 0xff).toByte(),
    (value.toInt() shr 8 and 0xff).toByte(),
  )

  private suspend fun updateStatus(
    bookId: BookId,
    status: GenerationStatus,
  ) {
    generationRepository.insert(
      GenerationProgress(
        bookId = bookId,
        status = status,
        lastUpdated = Instant.now(),
      ),
    )
  }

  public companion object {
    public const val KEY_BOOK_ID: String = "book_id"
  }

  public class Creator(
    private val characterRepository: CharacterRepository,
    private val voiceMappingRepository: VoiceMappingRepository,
    private val wordPronunciationRepository: WordPronunciationRepository,
    private val audioGenerationProgressRepository: AudioGenerationProgressRepository,
    private val generationRepository: GenerationRepository,
    private val bookContentRepo: BookContentRepo,
    private val geminiApi: GeminiApi,
    private val apiKeyStore: DataStore<String>,
    private val modelStore: DataStore<String>,
    private val mediaScanTrigger: MediaScanTrigger,
  ) : WorkerCreator {
    override fun create(
      context: Context,
      parameters: WorkerParameters,
    ): ListenableWorker {
      return GenerationWorker(
        context, parameters, characterRepository, voiceMappingRepository,
        wordPronunciationRepository, audioGenerationProgressRepository,
        generationRepository, bookContentRepo, geminiApi, apiKeyStore,
        modelStore, mediaScanTrigger,
      )
    }
  }
}
