package voice.core.gemini

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response

class GeminiClientTest {

  private val geminiApi: GeminiApi = mockk()
  private val client = GeminiClient(geminiApi, "fake-api-key")

  @Test
  fun `test parsing retryDelay from 429 error response`() = runTest {
    val errorJson = """
      {
        "error": {
          "code": 429,
          "message": "Quota exceeded",
          "status": "RESOURCE_EXHAUSTED",
          "details": [
            {
              "@type": "type.googleapis.com/google.rpc.RetryInfo",
              "retryDelay": "46s"
            }
          ]
        }
      }
    """.trimIndent()

    coEvery { geminiApi.generateContent(any(), any(), any()) } returns
      Response.error<GenerateContentResponse>(429, errorJson.toResponseBody("application/json".toMediaType()))

    val startTime = testScheduler.currentTime

    try {
        @Suppress("UNUSED_VARIABLE")
        val response = client.generateContent("model", GenerateContentRequest(emptyList()), maxRetries = 1)
    } catch (e: GeminiApiException) {
        assertEquals(429, e.code)
    }

    val duration = testScheduler.currentTime - startTime
    // maxRetries = 1 means 2 attempts total.
    // 1st attempt fails -> delay 46s -> 2nd attempt fails -> throw GeminiApiException
    assertEquals(46000L, duration)
  }

  @Test
  fun `test parsing retryDelay with decimal from 429 error response`() = runTest {
    val errorJson = """
      {
        "error": {
          "code": 429,
          "message": "Quota exceeded",
          "status": "RESOURCE_EXHAUSTED",
          "details": [
            {
              "@type": "type.googleapis.com/google.rpc.RetryInfo",
              "retryDelay": "46.748894752s"
            }
          ]
        }
      }
    """.trimIndent()

    coEvery { geminiApi.generateContent(any(), any(), any()) } returns
      Response.error<GenerateContentResponse>(429, errorJson.toResponseBody("application/json".toMediaType()))

    val startTime = testScheduler.currentTime

    try {
        @Suppress("UNUSED_VARIABLE")
        val response = client.generateContent("model", GenerateContentRequest(emptyList()), maxRetries = 1)
    } catch (e: GeminiApiException) {
        assertEquals(429, e.code)
    }

    val duration = testScheduler.currentTime - startTime
    // 46.748...s should round to 46s if we use toLong() or 47s if we round.
    // We use toDouble().toLong() in GeminiClient, which is 46.
    assertEquals(46000L, duration)
  }
}
