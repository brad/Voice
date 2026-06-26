package voice.core.work

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertTrue
import org.junit.Test
import voice.core.common.AppInfoProvider
import voice.core.gemini.GeminiApiException

class ErrorReportGeneratorTest {

  @Test
  fun `test report contains key information`() {
    val appInfoProvider = mockk<AppInfoProvider>()
    every { appInfoProvider.versionName } returns "1.2.3"

    val exception = Exception("Something went wrong")
    val report = ErrorReportGenerator.generate(
      throwable = exception,
      appInfoProvider = appInfoProvider,
      bookTitle = "Test Book",
      bookAuthor = "Test Author",
      step = "Processing"
    )

    assertTrue(report.contains("Error Report"))
    assertTrue(report.contains("Something went wrong"))
    assertTrue(report.contains("Test Book by Test Author"))
    assertTrue(report.contains("Processing"))
    assertTrue(report.contains("Version: 1.2.3"))
    assertTrue(report.contains("Stack Trace"))
  }

  @Test
  fun `test report contains Gemini API details`() {
    val appInfoProvider = mockk<AppInfoProvider>()
    every { appInfoProvider.versionName } returns "1.2.3"

    val exception = GeminiApiException(
      code = 400,
      statusMessage = "Bad Request",
      requestUrl = "https://api.gemini.com/v1/generate?key=***",
      requestBody = "GenerateContentRequest(model=gemini-pro)",
      responseBody = "Invalid request parameters"
    )

    val report = ErrorReportGenerator.generate(
      throwable = exception,
      appInfoProvider = appInfoProvider
    )

    assertTrue(report.contains("Gemini API Details"))
    assertTrue(report.contains("HTTP Code: 400"))
    assertTrue(report.contains("Status: Bad Request"))
    assertTrue(report.contains("URL: https://api.gemini.com/v1/generate?key=***"))
    assertTrue(report.contains("Response: Invalid request parameters"))
  }
}
