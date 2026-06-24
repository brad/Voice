package voice.features.generationSettings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import voice.core.common.rootGraphAs
import voice.core.data.BookId
import voice.core.data.Character
import voice.core.data.VoiceMapping
import voice.core.ui.icons.VoiceIcons
import voice.features.generationSettings.di.GenerationSettingsGraph
import voice.navigation.Destination
import voice.navigation.NavEntryProvider
import voice.core.strings.R as StringsR

@ContributesTo(AppScope::class)
public interface GenerationSettingsProvider {
  @Provides
  @IntoSet
  public fun generationSettingsNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.GenerationSettings> { key ->
    NavEntry(key) {
      GenerationSettingsScreen(bookId = key.bookId)
    }
  }
}

@Composable
internal fun GenerationSettingsScreen(bookId: BookId) {
  val viewModel = retain {
    rootGraphAs<GenerationSettingsGraph.Factory.Provider>()
      .generationSettingsGraphProviderFactory
      .createGenerationSettingsGraph()
      .generationSettingsViewModel
  }

  GenerationSettings(
    bookId = bookId,
    viewState = viewModel.state(bookId),
    onStartGeneration = viewModel::startGeneration,
    onClose = viewModel::close,
  )
}

@Composable
private fun GenerationSettings(
  bookId: BookId,
  viewState: GenerationSettingsViewState,
  onStartGeneration: (BookId) -> Unit,
  onClose: () -> Unit,
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Generation Settings") },
        navigationIcon = {
          IconButton(onClick = onClose) {
            Icon(
              imageVector = VoiceIcons.ArrowBack,
              contentDescription = stringResource(StringsR.string.common_action_close),
            )
          }
        },
      )
    },
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    bottomBar = {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(WindowInsets.systemBars.asPaddingValues())
          .padding(16.dp),
        contentAlignment = Alignment.Center,
      ) {
        Button(
          onClick = { onStartGeneration(bookId) },
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text("Start Generation")
        }
      }
    },
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
    ) {
      item {
        Text(
          text = "Characters & Voices",
          style = MaterialTheme.typography.titleMedium,
          modifier = Modifier.padding(16.dp),
        )
      }

      items(viewState.characters) { character ->
        val mapping = viewState.voiceMappings[character.id]
        CharacterVoiceItem(character, mapping)
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }
    }
  }
}

@Composable
private fun CharacterVoiceItem(
  character: Character,
  mapping: VoiceMapping?,
) {
  ListItem(
    headlineContent = { Text(character.name) },
    supportingContent = {
      Column {
        Text("Traits: ${character.gender}, ${character.age}, ${character.energy}")
        if (mapping != null) {
          Text("Voice: ${mapping.voiceName} (Speed: ${mapping.speed}, Pitch: ${mapping.pitch})")
        }
      }
    },
    leadingContent = {
      Icon(imageVector = VoiceIcons.Person, contentDescription = null)
    },
    trailingContent = {
      IconButton(onClick = { /* TODO: Edit voice settings */ }) {
        Icon(imageVector = VoiceIcons.Settings, contentDescription = "Edit")
      }
    },
  )
}
