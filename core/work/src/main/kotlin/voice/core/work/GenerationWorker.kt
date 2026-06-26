package voice.core.work

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import voice.core.common.AppInfoProvider
import voice.core.data.AudioGenerationProgress
import voice.core.data.BookId
import voice.core.data.Character
import voice.core.data.GenerationStatus
import voice.core.data.NarrationPiece
import voice.core.data.VoiceMapping
import voice.core.data.repo.AudioGenerationProgressRepository
import voice.core.data.repo.BookContentRepo
import voice.core.data.repo.CharacterRepository
import voice.core.data.repo.GenerationRepository
import voice.core.data.repo.NarrationPieceRepository
import voice.core.data.repo.VoiceMappingRepository
import voice.core.data.repo.WordPronunciationRepository
import voice.core.gemini.Content
import voice.core.gemini.GeminiApi
import voice.core.gemini.GeminiClient
import voice.core.gemini.GenerateContentRequest
import voice.core.gemini.GenerationConfig
import voice.core.gemini.Part
import voice.core.gemini.PrebuiltVoiceConfig
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
  private val narrationPieceRepository: NarrationPieceRepository,
  private val audioGenerationProgressRepository: AudioGenerationProgressRepository,
  private val generationRepository: GenerationRepository,
  private val bookContentRepo: BookContentRepo,
  private val geminiApi: GeminiApi,
  private val apiKeyStore: DataStore<String>,
  private val modelStore: DataStore<String>,
  private val mediaScanTrigger: MediaScanTrigger,
  private val appInfoProvider: AppInfoProvider,
) : CoroutineWorker(context, params) {

  override suspend fun doWork(): Result {
    val bookIdString = inputData.getString(KEY_BOOK_ID) ?: return Result.failure()
    val bookId = BookId(bookIdString)

    var currentTitle: String? = null
    var currentAuthor: String? = null
    var currentChapterIdx: Int? = null
    var currentPieceIdx: Int? = null

    try {
      val apiKey = apiKeyStore.data.first()
      if (apiKey.isBlank()) {
        Logger.e("Gemini API key is missing")
        updateStatus(bookId, GenerationStatus.FAILED, "Gemini API key is missing")
        return Result.failure()
      }

      updateStatus(bookId, GenerationStatus.GENERATING)

      val model = modelStore.data.first().ifBlank { "gemini-3.1-flash-tts-preview" }
      val client = GeminiClient(geminiApi, apiKey)

      val generationProgress = generationRepository.progressForBook(bookId)
      currentTitle = generationProgress?.title
      currentAuthor = generationProgress?.author

      val characters = characterRepository.charactersForBook(bookId)
      val mappings = voiceMappingRepository.mappingsForBook(bookId)
      val pronunciations = wordPronunciationRepository.pronunciationsForBook(bookId)
      val narrationPieces = narrationPieceRepository.getForBook(bookId)

      if (narrationPieces.isEmpty()) {
        Logger.e("No narration pieces found for book ${bookId.value}")
        updateStatus(bookId, GenerationStatus.FAILED, "No narration pieces found.")
        return Result.failure()
      }

      val progress = audioGenerationProgressRepository.progressForBook(bookId)
      val startPieceIndex = progress?.chunkIndex ?: 0 // Using chunkIndex as pieceIndex

      val outputDir = File(applicationContext.filesDir, "audiobooks/${bookId.value}")
      outputDir.mkdirs()

      val pronunciationInstructions = if (pronunciations.isNotEmpty()) {
        "\n\nPronunciation Guide:\n" + pronunciations.joinToString("\n") { "${it.word} -> ${it.phonetic}" }
      } else {
        ""
      }

      // Group pieces into chapters
      val chapters = mutableListOf<List<NarrationPiece>>()
      var currentChapterPieces = mutableListOf<NarrationPiece>()
      for (piece in narrationPieces) {
        if (piece.isNewChapter && currentChapterPieces.isNotEmpty()) {
          chapters.add(currentChapterPieces)
          currentChapterPieces = mutableListOf()
        }
        currentChapterPieces.add(piece)
      }
      if (currentChapterPieces.isNotEmpty()) {
        chapters.add(currentChapterPieces)
      }

      // Find which chapter to start from
      var piecesProcessed = 0
      var startChapterIdx = 0
      for (i in chapters.indices) {
        if (piecesProcessed + chapters[i].size > startPieceIndex) {
          startChapterIdx = i
          break
        }
        piecesProcessed += chapters[i].size
      }

      for (chapterIdx in startChapterIdx until chapters.size) {
        currentChapterIdx = chapterIdx
        val chapterPieces = chapters[chapterIdx]
        val chapterChunks = mutableListOf<File>()

        // Find start piece in this chapter
        val chapterStartPieceIdx = if (chapterIdx == startChapterIdx) startPieceIndex - piecesProcessed else 0

        // Group pieces in chapter by character, up to 4000 chars
        var j = chapterStartPieceIdx
        while (j < chapterPieces.size) {
          currentPieceIdx = piecesProcessed + j
          val firstPiece = chapterPieces[j]
          val groupPieces = mutableListOf(firstPiece)
          var groupText = firstPiece.text
          j++

          while (j < chapterPieces.size &&
                 chapterPieces[j].characterName == firstPiece.characterName &&
                 !chapterPieces[j].isNewChapter &&
                 groupText.length + chapterPieces[j].text.length < 4000) {
            groupPieces.add(chapterPieces[j])
            groupText += "\n" + chapterPieces[j].text
            j++
          }

          val character = characters.find { it.name == firstPiece.characterName }
          val mapping = mappings.find { it.characterId == character?.id }
          val tuning = if (mapping != null) {
            " (Speed: ${mapping.speed}, Pitch: ${mapping.pitch}, Energy: ${mapping.energy})"
          } else {
            ""
          }

          val speakerPrompt = """
            Perform a TTS generation for the following text.
            Character Profile: ${firstPiece.characterName}: ${character?.personality ?: "Narrator"}$tuning$pronunciationInstructions

            Text:
            $groupText
          """.trimIndent()

          val voiceName = mapping?.voiceName ?: "Charon"
          val speechConfig = SpeechConfig(
            voiceConfig = VoiceConfig(
              prebuiltVoiceConfig = PrebuiltVoiceConfig(voiceName = voiceName),
            ),
          )

          val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = speakerPrompt)))),
            generationConfig = GenerationConfig(
              responseModalities = listOf("AUDIO"),
              speechConfig = speechConfig,
            ),
          )

          val response = client.generateContent(model, request)
          val audioData = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.inlineData?.data
            ?: throw Exception("No audio data in response")

          val chunkFile = File(outputDir, "ch_${chapterIdx}_piece_${piecesProcessed + j - groupPieces.size}.raw")
          FileOutputStream(chunkFile).use { fos ->
            fos.write(Base64.decode(audioData, Base64.DEFAULT))
          }
          chapterChunks.add(chunkFile)

          audioGenerationProgressRepository.insert(
            AudioGenerationProgress(
              bookId = bookId,
              chapterIndex = chapterIdx,
              chunkIndex = piecesProcessed + j,
              totalChunks = narrationPieces.size,
              lastUpdated = Instant.now(),
            ),
          )
        }

        // Merge chunks into chapter WAV
        if (chapterChunks.isNotEmpty()) {
          val chapterFile = File(outputDir, "chapter_$chapterIdx.wav")
          mergePcmFilesToWav(chapterChunks, chapterFile)
          chapterChunks.forEach { it.delete() }
        }

        piecesProcessed += chapterPieces.size
      }

      updateStatus(bookId, GenerationStatus.COMPLETED)
      mediaScanTrigger.scan()

      // Improve metadata after scan
      val internalId = BookId(outputDir.toURI().toString())
      val generatedContent = bookContentRepo.get(internalId)
      if (generatedContent != null) {
        bookContentRepo.put(generatedContent.copy(name = currentTitle ?: "Unknown Audiobook"))
      }

      return Result.success()
    } catch (e: Exception) {
      Logger.e(e, "Error generating audio")
      if (runAttemptCount >= 3) {
        val report = ErrorReportGenerator.generate(
          throwable = e,
          appInfoProvider = appInfoProvider,
          bookTitle = currentTitle,
          bookAuthor = currentAuthor,
          step = "Audio Generation (Ch $currentChapterIdx, Piece $currentPieceIdx)",
        )
        updateStatus(bookId, GenerationStatus.FAILED, report)
        return Result.failure()
      }
      return Result.retry()
    }
  }

  private fun mergePcmFilesToWav(
    pcmFiles: List<File>,
    outputFile: File,
  ) {
    val sampleRate = 24000
    val channels = 1
    val bitDepth = 16

    var totalAudioLen = 0L
    pcmFiles.forEach { totalAudioLen += it.length() }

    val totalDataLen = totalAudioLen + 36
    val byteRate = (sampleRate * channels * bitDepth / 8).toLong()

    FileOutputStream(outputFile).use { out ->
      out.write("RIFF".toByteArray())
      out.write(intToByteArray(totalDataLen.toInt()))
      out.write("WAVE".toByteArray())

      out.write("fmt ".toByteArray())
      out.write(intToByteArray(16))
      out.write(shortToByteArray(1))
      out.write(shortToByteArray(channels.toShort()))
      out.write(intToByteArray(sampleRate))
      out.write(intToByteArray(byteRate.toInt()))
      out.write(shortToByteArray((channels * bitDepth / 8).toShort()))
      out.write(shortToByteArray(bitDepth.toShort()))

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
    errorMessage: String? = null,
  ) {
    generationRepository.updateStatus(
      bookId = bookId,
      status = status,
      lastUpdated = Instant.now(),
      errorMessage = errorMessage,
    )
  }

  public companion object {
    public const val KEY_BOOK_ID: String = "book_id"
  }

  public class Creator(
    private val characterRepository: CharacterRepository,
    private val voiceMappingRepository: VoiceMappingRepository,
    private val wordPronunciationRepository: WordPronunciationRepository,
    private val narrationPieceRepository: NarrationPieceRepository,
    private val audioGenerationProgressRepository: AudioGenerationProgressRepository,
    private val generationRepository: GenerationRepository,
    private val bookContentRepo: BookContentRepo,
    private val geminiApi: GeminiApi,
    private val apiKeyStore: DataStore<String>,
    private val modelStore: DataStore<String>,
    private val mediaScanTrigger: MediaScanTrigger,
    private val appInfoProvider: AppInfoProvider,
  ) : WorkerCreator {
    override fun create(
      context: Context,
      parameters: WorkerParameters,
    ): ListenableWorker {
      return GenerationWorker(
        context, parameters, characterRepository, voiceMappingRepository,
        wordPronunciationRepository, narrationPieceRepository,
        audioGenerationProgressRepository, generationRepository,
        bookContentRepo, geminiApi, apiKeyStore, modelStore,
        mediaScanTrigger, appInfoProvider,
      )
    }
  }
}
