package voice.core.gemini

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GeminiApi {
  @POST("v1beta/models/{model}:generateContent")
  suspend fun generateContent(
    @Path("model") model: String,
    @Query("key") apiKey: String,
    @Body request: GenerateContentRequest,
  ): Response<GenerateContentResponse>
}

@Serializable
data class GenerateContentRequest(
  val contents: List<Content>,
  val generationConfig: GenerationConfig? = null,
)

@Serializable
data class Content(
  val parts: List<Part>,
)

@Serializable
data class Part(
  val text: String? = null,
  val inlineData: InlineData? = null,
)

@Serializable
data class InlineData(
  val mimeType: String,
  val data: String,
)

@Serializable
data class GenerationConfig(
  val responseMimeType: String? = null,
  val responseSchema: ResponseSchema? = null,
  val speechConfig: SpeechConfig? = null,
  val responseModalities: List<String>? = null,
)

@Serializable
data class SpeechConfig(
  val voiceConfig: VoiceConfig,
)

@Serializable
data class VoiceConfig(
  val prebuiltVoiceConfig: PrebuiltVoiceConfig,
)

@Serializable
data class PrebuiltVoiceConfig(
  val voiceName: String,
)

@Serializable
data class ResponseSchema(
  val type: String,
  val properties: Map<String, SchemaProperty>? = null,
  val required: List<String>? = null,
  val items: ResponseSchema? = null,
)

@Serializable
data class SchemaProperty(
  val type: String,
  val description: String? = null,
  val enum: List<String>? = null,
)

@Serializable
data class GenerateContentResponse(
  val candidates: List<Candidate>,
)

@Serializable
data class Candidate(
  val content: Content,
)
