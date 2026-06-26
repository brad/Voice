package voice.core.gemini

import java.io.IOException

public class GeminiApiException(
  public val code: Int,
  public val statusMessage: String?,
  public val requestUrl: String,
  public val requestBody: String?,
  public val responseBody: String?,
  cause: Throwable? = null,
) : IOException("Gemini API error: $code $statusMessage\nURL: $requestUrl\nResponse: $responseBody", cause)
