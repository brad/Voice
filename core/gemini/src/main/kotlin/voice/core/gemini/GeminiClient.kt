package voice.core.gemini

import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import voice.core.logging.api.Logger
import java.io.IOException

public class GeminiClient(
  private val api: GeminiApi,
  private val apiKey: String,
) {
  private val json = Json { ignoreUnknownKeys = true }
  private val prettyJson = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
  }

  public suspend fun generateContent(
    model: String,
    request: GenerateContentRequest,
    maxRetries: Int = 5,
    onRetry: suspend (Long) -> Unit = {},
  ): GenerateContentResponse {
    var retryCount = 0
    val requestBodyJson = try {
      prettyJson.encodeToString(request)
    } catch (e: Exception) {
      "Error encoding request: ${e.message}"
    }

    while (true) {
      val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent"
      val maskedUrl = "$baseUrl?key=***"

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

        throw GeminiApiException(
          code = response.code(),
          statusMessage = response.message(),
          requestUrl = maskedUrl,
          requestBody = requestBodyJson,
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

        // Wrap other exceptions (like timeout or networking after retries) to include request context
        throw GeminiApiException(
          code = -1,
          statusMessage = e.message ?: e.javaClass.simpleName,
          requestUrl = maskedUrl,
          requestBody = requestBodyJson,
          responseBody = null,
          cause = e,
        )
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
    val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models"
    val maskedUrl = "$baseUrl?key=***"

    if (response.isSuccessful) {
      return response.body()?.models ?: emptyList()
    }

    val errorBody = response.errorBody()?.string()

    throw GeminiApiException(
      code = response.code(),
      statusMessage = response.message(),
      requestUrl = maskedUrl,
      requestBody = null,
      responseBody = errorBody,
    )
  }
}
