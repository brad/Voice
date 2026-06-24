package voice.core.gemini

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

public interface GeminiApi {
  @POST("v1beta/models/{model}:generateContent")
  public suspend fun generateContent(
    @Path("model") model: String,
    @Query("key") apiKey: String,
    @Body request: GenerateContentRequest,
  ): Response<GenerateContentResponse>

  @GET("v1beta/models")
  public suspend fun listModels(
    @Query("key") apiKey: String,
  ): Response<ListModelsResponse>
}

@Serializable
public data class GenerateContentRequest(
  val contents: List<Content>,
  val generationConfig: GenerationConfig? = null,
)

@Serializable
public data class Content(val parts: List<Part>)

@Serializable
public data class Part(
  val text: String? = null,
  val inlineData: InlineData? = null,
)

@Serializable
public data class InlineData(
  val mimeType: String,
  val data: String,
)

@Serializable
public data class GenerationConfig(
  val responseMimeType: String? = null,
  val responseSchema: ResponseSchema? = null,
  val speechConfig: SpeechConfig? = null,
  val responseModalities: List<String>? = null,
)

@Serializable
public data class SpeechConfig(
  val voiceConfig: VoiceConfig? = null,
  val multiSpeakerVoiceConfig: MultiSpeakerVoiceConfig? = null,
)

@Serializable
public data class MultiSpeakerVoiceConfig(val speakerVoiceConfigs: List<SpeakerVoiceConfig>)

@Serializable
public data class SpeakerVoiceConfig(
  val speaker: String,
  val voiceConfig: VoiceConfig,
)

@Serializable
public data class VoiceConfig(val prebuiltVoiceConfig: PrebuiltVoiceConfig)

@Serializable
public data class PrebuiltVoiceConfig(val voiceName: String)

@Serializable
public data class ResponseSchema(
  val type: String,
  val description: String? = null,
  val properties: Map<String, ResponseSchema>? = null,
  val required: List<String>? = null,
  val items: ResponseSchema? = null,
  val enum: List<String>? = null,
)

@Serializable
public data class GenerateContentResponse(val candidates: List<Candidate>)

@Serializable
public data class Candidate(val content: Content)

@Serializable
public data class ListModelsResponse(val models: List<Model>)

@Serializable
public data class Model(
  val name: String,
  val baseModelId: String? = null,
  val version: String? = null,
  val displayName: String? = null,
  val description: String? = null,
  val supportedGenerationMethods: List<String>? = null,
)
