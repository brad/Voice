package voice.core.work

import android.os.Build
import voice.core.common.AppInfoProvider
import voice.core.gemini.GeminiApiException
import voice.core.logging.api.Logger

public object ErrorReportGenerator {
  public fun generate(
    throwable: Throwable,
    appInfoProvider: AppInfoProvider,
    bookTitle: String? = null,
    bookAuthor: String? = null,
    step: String? = null,
  ): String {
    val sb = StringBuilder()
    sb.appendLine("Error Report")
    sb.appendLine("============")
    sb.appendLine("Message: ${throwable.message}")
    if (step != null) sb.appendLine("Step: $step")
    if (bookTitle != null) sb.appendLine("Book: $bookTitle by ${bookAuthor ?: "Unknown"}")
    sb.appendLine()

    sb.appendLine("Device Info")
    sb.appendLine("-----------")
    sb.appendLine("Manufacturer: ${Build.MANUFACTURER}")
    sb.appendLine("Model: ${Build.MODEL}")
    sb.appendLine("OS Version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
    sb.appendLine()

    sb.appendLine("App Info")
    sb.appendLine("--------")
    sb.appendLine("Version: ${appInfoProvider.versionName}")
    sb.appendLine()

    if (throwable is GeminiApiException) {
      sb.appendLine("Gemini API Details")
      sb.appendLine("------------------")
      sb.appendLine("HTTP Code: ${throwable.code}")
      sb.appendLine("Status: ${throwable.statusMessage}")
      sb.appendLine("URL: ${throwable.requestUrl}")
      sb.appendLine("Request: ${throwable.requestBody}")
      sb.appendLine("Response: ${throwable.responseBody}")
      sb.appendLine()
    }

    sb.appendLine("Stack Trace")
    sb.appendLine("-----------")
    sb.appendLine(Logger.getStackTraceString(throwable))

    return sb.toString()
  }
}
