package voice.features.settings

import androidx.datastore.core.DataStore
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import app.cash.turbine.test
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import retrofit2.Response
import voice.core.common.AppInfoProvider
import voice.core.common.DispatcherProvider
import voice.core.data.GridMode
import voice.core.data.ThemeColorScheme
import voice.core.data.ThemeMode
import voice.core.data.sleeptimer.SleepTimerPreference
import voice.core.featureflag.MemoryFeatureFlag
import voice.core.gemini.GeminiApi
import voice.core.gemini.ListModelsResponse
import voice.core.gemini.Model
import voice.core.ui.DynamicColorAvailability
import voice.core.ui.GridCount
import voice.navigation.Destination
import voice.navigation.Navigator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant

class SettingsViewModelTest {

  private val scope = TestScope()
  private val themeModeStore = MemoryDataStore(ThemeMode.FollowSystem)
  private val themeColorSchemeStore = MemoryDataStore(ThemeColorScheme.VoiceBlue)
  private val autoRewindAmountStore = MemoryDataStore(10)
  private val seekTimeStore = MemoryDataStore(30)
  private val gridModeStore = MemoryDataStore(GridMode.GRID)
  private val sleepTimerPreferenceStore = MemoryDataStore(SleepTimerPreference.Default)
  private val analyticsConsentStore = MemoryDataStore(false)
  private val developerMenuUnlockedStore = MemoryDataStore(false)
  private val geminiApiKeyStore = MemoryDataStore("")
  private val geminiAnalysisModelStore = MemoryDataStore("gemini-3.1-flash-lite")
  private val geminiGenerationModelStore = MemoryDataStore("gemini-3.1-flash-tts-preview")
  private val navigator = mockk<Navigator> {
    every { goTo(any()) } just Runs
  }
  private val appInfoProvider = mockk<AppInfoProvider> {
    every { versionName } returns "1.2.3"
    every { analyticsIncluded } returns true
    every { supportDevelopmentIncluded } returns true
    every { installTime } returns Instant.parse("2026-06-01T00:00:00Z")
  }
  private val gridCount = mockk<GridCount> {
    every { useGridAsDefault() } returns true
  }
  private val kioskModeFeatureFlag = MemoryFeatureFlag(false)
  private val dynamicColorAvailability = mockk<DynamicColorAvailability> {
    every { isSupported() } returns true
  }
  private val geminiApi = mockk<GeminiApi> {
    coEvery { listModels(any()) } returns Response.success(ListModelsResponse(listOf(Model(name = "models/gemini-3.1-flash-lite"))))
  }

  private val viewModel = SettingsViewModel(
    themeModeStore = themeModeStore,
    themeColorSchemeStore = themeColorSchemeStore,
    autoRewindAmountStore = autoRewindAmountStore,
    seekTimeStore = seekTimeStore,
    navigator = navigator,
    appInfoProvider = appInfoProvider,
    gridModeStore = gridModeStore,
    sleepTimerPreferenceStore = sleepTimerPreferenceStore,
    analyticsConsentStore = analyticsConsentStore,
    gridCount = gridCount,
    kioskModeFeatureFlag = kioskModeFeatureFlag,
    developerMenuUnlockedStore = developerMenuUnlockedStore,
    dynamicColorAvailability = dynamicColorAvailability,
    geminiApiKeyStore = geminiApiKeyStore,
    geminiAnalysisModelStore = geminiAnalysisModelStore,
    geminiGenerationModelStore = geminiGenerationModelStore,
    geminiApi = geminiApi,
    dispatcherProvider = DispatcherProvider(scope.coroutineContext, scope.coroutineContext, scope.coroutineContext),
  )

  @Test
  fun `view state defaults to follow system and voice blue`() = scope.runTest {
    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.viewState()
    }.test {
      awaitItem().let {
        assertEquals(expected = ThemeMode.FollowSystem, actual = it.themeMode)
        assertEquals(expected = ThemeColorScheme.VoiceBlue, actual = it.themeColorScheme)
      }
    }
  }

  @Test
  fun `theme mode changes update view state`() = scope.runTest {
    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.viewState()
    }.test {
      assertEquals(expected = ThemeMode.FollowSystem, actual = awaitItem().themeMode)

      viewModel.setThemeMode(ThemeMode.Dark)
      assertEquals(expected = ThemeMode.Dark, actual = awaitItem().themeMode)

      viewModel.setThemeMode(ThemeMode.Light)
      assertEquals(expected = ThemeMode.Light, actual = awaitItem().themeMode)

      viewModel.setThemeMode(ThemeMode.FollowSystem)
      assertEquals(expected = ThemeMode.FollowSystem, actual = awaitItem().themeMode)
    }
  }

  @Test
  fun `color scheme setting is visible when dynamic color is supported`() = scope.runTest {
    every { dynamicColorAvailability.isSupported() } returns true

    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.viewState()
    }.test {
      assertEquals(expected = true, actual = awaitItem().showThemeColorSchemePref)
    }
  }

  @Test
  fun `color scheme setting is hidden when dynamic color is unsupported`() = scope.runTest {
    every { dynamicColorAvailability.isSupported() } returns false

    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.viewState()
    }.test {
      assertEquals(expected = false, actual = awaitItem().showThemeColorSchemePref)
    }
  }

  @Test
  fun `selecting dynamic color updates view state`() = scope.runTest {
    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.viewState()
    }.test {
      assertEquals(expected = ThemeColorScheme.VoiceBlue, actual = awaitItem().themeColorScheme)

      viewModel.setThemeColorScheme(ThemeColorScheme.Dynamic)

      assertEquals(expected = ThemeColorScheme.Dynamic, actual = awaitItem().themeColorScheme)
    }
  }

  @Test
  fun `developer menu is hidden until app version tapped 13 times`() = scope.runTest {
    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.viewState()
    }.test {
      assertEquals(expected = false, actual = awaitItem().showDeveloperMenu)

      repeat(13) {
        viewModel.onAppVersionClick()
      }

      assertEquals(expected = true, actual = awaitItem().showDeveloperMenu)
    }
  }

  @Test
  fun `developer menu unlock emits snackbar effect`() = scope.runTest {
    viewModel.viewEffects.test {
      repeat(13) {
        viewModel.onAppVersionClick()
      }

      assertIs<SettingsViewEffect.DeveloperMenuUnlocked>(awaitItem())
    }
  }

  @Test
  fun `openDeveloperMenu navigates to developer settings`() {
    viewModel.openDeveloperMenu()

    verify(exactly = 1) {
      navigator.goTo(Destination.DeveloperSettings)
    }
  }

  @Test
  fun `openSupportVoice navigates to support screen`() {
    viewModel.openSupportVoice()

    verify(exactly = 1) {
      navigator.goTo(Destination.SupportVoice)
    }
  }

  @Test
  fun `openFolderPicker navigates to folder picker`() {
    viewModel.openFolderPicker()

    verify(exactly = 1) {
      navigator.goTo(Destination.FolderPicker)
    }
  }

  @Test
  fun `view state shows support development when included`() = scope.runTest {
    every { appInfoProvider.supportDevelopmentIncluded } returns true

    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.viewState()
    }.test {
      assertEquals(expected = true, actual = awaitItem().showSupportDevelopment)
    }
  }

  @Test
  fun `view state hides support development when not included`() = scope.runTest {
    every { appInfoProvider.supportDevelopmentIncluded } returns false

    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.viewState()
    }.test {
      assertEquals(expected = false, actual = awaitItem().showSupportDevelopment)
    }
  }

  @Test
  fun `view state exposes kiosk mode`() = scope.runTest {
    kioskModeFeatureFlag.value = true

    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.viewState()
    }.test {
      awaitItem().let {
        assertEquals(expected = true, actual = it.kioskMode)
      }
    }
  }

  @Test
  fun `audiobook generation settings changes update view state`() = scope.runTest {
    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.viewState()
    }.test {
      val initial = awaitItem()
      assertEquals(expected = "", actual = initial.geminiApiKey)
      assertEquals(expected = "gemini-3.1-flash-lite", actual = initial.geminiAnalysisModel)
      assertEquals(expected = "gemini-3.1-flash-tts-preview", actual = initial.geminiGenerationModel)

      viewModel.saveAudiobookGenerationSettings("new-key", "new-analysis", "new-gen")

      val updated = awaitItem()
      assertEquals(expected = "new-key", actual = updated.geminiApiKey)
      assertEquals(expected = "new-analysis", actual = updated.geminiAnalysisModel)
      assertEquals(expected = "new-gen", actual = updated.geminiGenerationModel)
    }
  }

  @Test
  fun `available models are fetched when api key is set`() = scope.runTest {
    backgroundScope.launchMolecule(RecompositionMode.Immediate) {
      viewModel.viewState()
    }.test {
      assertEquals(expected = emptyList(), actual = awaitItem().availableModels)

      viewModel.saveAudiobookGenerationSettings("valid-key", "gemini-3.1-flash-lite", "gemini-3.1-flash-tts-preview")

      // Wait for models to be fetched
      val updated = awaitItem()
      if (updated.availableModels.isEmpty()) {
        assertEquals(expected = listOf("gemini-3.1-flash-lite"), actual = awaitItem().availableModels)
      } else {
        assertEquals(expected = listOf("gemini-3.1-flash-lite"), actual = updated.availableModels)
      }
    }
  }
}

private class MemoryDataStore<T>(initial: T) : DataStore<T> {

  private val value = MutableStateFlow(initial)

  override val data: Flow<T> get() = value

  override suspend fun updateData(transform: suspend (t: T) -> T): T {
    return value.updateAndGet { transform(it) }
  }
}
