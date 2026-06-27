package voice.core.gemini

import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import voice.core.logging.api.Logger
import java.io.IOException

public class GeminiClient(
  private val api: GeminiApi,
  private val apiKey: String,
) {
  private val json = Json { ignoreUnknownKeys = true }

  public suspend fun generateContent(
    model: String,
    request: GenerateContentRequest,
    maxRetries: Int = 5,
    onRetry: suspend (Long) -> Unit = {},
  ): GenerateContentResponse {
    var retryCount = 0
    while (true) {
      try {
        val response = api.generateContent(model, apiKey, request)
        if (response.isSuccessful) {
          return response.body() ?: throw IOException("Empty response body")
        }

        val code = response.code()
        val errorBody = response.errorBody()?.string()

        if (code == 529 || code == 429) {
          val retryAfter = parseRetryAfter(errorBody) ?: response.headers()["Retry-After"]?.toLongOrNull() ?: (2L shl retryCount)
          if (retryCount < maxRetries) {
            Logger.w("Gemini API error $code. Retrying in $retryAfter seconds...")
            onRetry(retryAfter)
            delay(retryAfter * 1000)
            onRetry(0)
            retryCount++
            continue
          }
        }

        val requestUrl = response.raw().request.url.toString()
        // We don't log the full request body here to avoid leaking the API key if it's in the URL,
        // but it's passed in the query param "key" which we should ideally mask.
        val maskedUrl = requestUrl.replace(Regex("key=[^&]+"), "key=***")

        throw GeminiApiException(
          code = response.code(),
          statusMessage = response.message(),
          requestUrl = maskedUrl,
          requestBody = "GenerateContentRequest(model=$model)", // Simplified for now
          responseBody = errorBody,
        )
      } catch (e: Exception) {
        if (e is GeminiApiException) throw e
        if (retryCount < maxRetries && e is IOException) {
          val backoff = (2L shl retryCount)
          Logger.w(e, "Gemini API network error. Retrying in $backoff seconds...")
          onRetry(backoff)
          delay(backoff * 1000)
          onRetry(0)
          retryCount++
          continue
        }
        throw e
      }
    }
  }

  private fun parseRetryAfter(errorBody: String?): Long? {
    if (errorBody == null) return null
    return try {
      val errorResponse = json.decodeFromString<GeminiErrorResponse>(errorBody)
      val retryDelayStr = errorResponse.error.details
        ?.find { it.type == "type.googleapis.com/google.rpc.RetryInfo" }
        ?.retryDelay

      retryDelayStr?.removeSuffix("s")?.toDoubleOrNull()?.toLong()
    } catch (e: Exception) {
      null
    }
  }

  public suspend fun listModels(): List<Model> {
    val response = api.listModels(apiKey)
    if (response.isSuccessful) {
      return response.body()?.models ?: emptyList()
    }

    val errorBody = response.errorBody()?.string()
    val requestUrl = response.raw().request.url.toString()
    val maskedUrl = requestUrl.replace(Regex("key=[^&]+"), "key=***")

    throw GeminiApiException(
      code = response.code(),
      statusMessage = response.message(),
      requestUrl = maskedUrl,
      requestBody = null,
      responseBody = errorBody,
    )
  }
}
