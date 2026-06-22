package voice.core.gemini

import kotlinx.coroutines.delay
import voice.core.logging.api.Logger
import java.io.IOException

class GeminiClient(
    private val api: GeminiApi,
    private val apiKey: String
) {
    suspend fun generateContent(
        model: String,
        request: GenerateContentRequest,
        maxRetries: Int = 5
    ): GenerateContentResponse {
        var retryCount = 0
        while (true) {
            try {
                val response = api.generateContent(model, apiKey, request)
                if (response.isSuccessful) {
                    return response.body() ?: throw IOException("Empty response body")
                }

                val code = response.code()
                if (code == 529 || code == 429) {
                    val retryAfter = response.headers()["Retry-After"]?.toLongOrNull() ?: (2L shl retryCount)
                    if (retryCount < maxRetries) {
                        Logger.w("Gemini API error $code. Retrying in $retryAfter seconds...")
                        delay(retryAfter * 1000)
                        retryCount++
                        continue
                    }
                }
                throw IOException("Gemini API error: ${response.code()} ${response.message()}")
            } catch (e: Exception) {
                if (retryCount < maxRetries && e is IOException) {
                    val backoff = (2L shl retryCount)
                    Logger.w(e, "Gemini API network error. Retrying in $backoff seconds...")
                    delay(backoff * 1000)
                    retryCount++
                    continue
                }
                throw e
            }
        }
    }
}
