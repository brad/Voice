package voice.features.generationSettings.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import voice.features.generationSettings.GenerationSettingsViewModel

abstract class GenerationSettingsScope private constructor()

@GraphExtension(scope = GenerationSettingsScope::class)
public interface GenerationSettingsGraph {
  public val generationSettingsViewModel: GenerationSettingsViewModel

  @GraphExtension.Factory
  @ContributesTo(AppScope::class)
  public interface Factory {
    public fun createGenerationSettingsGraph(): GenerationSettingsGraph

    @ContributesTo(AppScope::class)
    public interface Provider {
      public val generationSettingsGraphProviderFactory: Factory
    }
  }
}
