package voice.features.settings

import voice.core.data.ThemeColorScheme
import voice.core.data.ThemeMode
import java.time.LocalTime

public interface SettingsListener {
  public fun close()
  public fun onThemeModeRowClick()
  public fun onThemeColorSchemeRowClick()
  public fun setThemeMode(themeMode: ThemeMode)
  public fun setThemeColorScheme(themeColorScheme: ThemeColorScheme)
  public fun toggleGrid()
  public fun seekAmountChanged(seconds: Int)
  public fun onSeekAmountRowClick()
  public fun autoRewindAmountChang(seconds: Int)
  public fun onAutoRewindRowClick()
  public fun dismissDialog()
  public fun getSupport()
  public fun suggestIdea()
  public fun openBugReport()
  public fun openTranslations()
  public fun openFaq()
  public fun openSupportVoice()
  public fun setAutoSleepTimer(checked: Boolean)
  public fun setAutoSleepTimerStart(time: LocalTime)
  public fun setAutoSleepTimerEnd(time: LocalTime)
  public fun toggleAnalytics()
  public fun openFolderPicker()
  public fun onAppVersionClick()

  public fun openDeveloperMenu()

  public fun onAudiobookGenerationRowClick()
  public fun saveAudiobookGenerationSettings(
    apiKey: String,
    analysisModel: String,
    generationModel: String,
  )

  public companion object {
    public fun noop(): SettingsListener = object : SettingsListener {
      override fun close() {}
      override fun onThemeModeRowClick() {}
      override fun onThemeColorSchemeRowClick() {}
      override fun setThemeMode(themeMode: ThemeMode) {}
      override fun setThemeColorScheme(themeColorScheme: ThemeColorScheme) {}
      override fun toggleGrid() {}
      override fun seekAmountChanged(seconds: Int) {}
      override fun onSeekAmountRowClick() {}
      override fun autoRewindAmountChang(seconds: Int) {}
      override fun onAutoRewindRowClick() {}
      override fun dismissDialog() {}
      override fun getSupport() {}
      override fun suggestIdea() {}
      override fun openBugReport() {}
      override fun openTranslations() {}
      override fun openFaq() {}
      override fun openSupportVoice() {}
      override fun setAutoSleepTimer(checked: Boolean) {}
      override fun setAutoSleepTimerStart(time: LocalTime) {}
      override fun setAutoSleepTimerEnd(time: LocalTime) {}
      override fun toggleAnalytics() {}
      override fun openFolderPicker() {}
      override fun onAppVersionClick() {}
      override fun openDeveloperMenu() {}
      override fun onAudiobookGenerationRowClick() {}
      override fun saveAudiobookGenerationSettings(
        apiKey: String,
        analysisModel: String,
        generationModel: String,
      ) {}
    }
  }
}
