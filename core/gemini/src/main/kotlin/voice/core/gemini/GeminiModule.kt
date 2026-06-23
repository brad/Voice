package voice.core.gemini

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create

@ContributesTo(AppScope::class)
public interface GeminiModule {

  @Provides
  @SingleIn(AppScope::class)
  public fun provideGeminiApi(okHttpClient: OkHttpClient): GeminiApi {
    val json = Json { ignoreUnknownKeys = true }
    val contentType = "application/json".toMediaType()
    return Retrofit.Builder()
      .baseUrl("https://generativelanguage.googleapis.com/")
      .client(okHttpClient)
      .addConverterFactory(json.asConverterFactory(contentType))
      .build()
      .create()
  }
}
