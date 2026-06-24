package voice.features.generationSettings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
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
import voice.core.data.GenerationStatus
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
  var showDiscardDialog by remember { mutableStateOf(false) }

  if (showDiscardDialog) {
    AlertDialog(
      onDismissRequest = { showDiscardDialog = false },
      title = { Text(stringResource(StringsR.string.generation_settings_discard_confirmation_title)) },
      text = { Text(stringResource(StringsR.string.generation_settings_discard_confirmation_message)) },
      confirmButton = {
        TextButton(
          onClick = {
            viewState.onDiscardAndRestart()
            showDiscardDialog = false
          },
        ) {
          Text(stringResource(StringsR.string.generation_settings_discard_confirmation_confirm))
        }
      },
      dismissButton = {
        TextButton(onClick = { showDiscardDialog = false }) {
          Text(stringResource(StringsR.string.generation_settings_discard_confirmation_cancel))
        }
      },
    )
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(stringResource(StringsR.string.generation_settings_title)) },
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
      val isGenerating = viewState.status == GenerationStatus.GENERATING
      val isCompleted = viewState.status == GenerationStatus.COMPLETED

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(WindowInsets.systemBars.asPaddingValues())
          .padding(16.dp),
        contentAlignment = Alignment.Center,
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          if (isGenerating) {
            Button(
              onClick = { },
              enabled = false,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(stringResource(StringsR.string.generation_settings_status_generating))
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
              onClick = { showDiscardDialog = true },
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(stringResource(StringsR.string.generation_settings_action_discard_restart))
            }
          } else if (isCompleted) {
             Text(
               text = stringResource(StringsR.string.generation_settings_status_completed),
               style = MaterialTheme.typography.bodyMedium,
               modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp),
             )
             Button(
               onClick = { showDiscardDialog = true },
               modifier = Modifier.fillMaxWidth(),
             ) {
               Text(stringResource(StringsR.string.generation_settings_action_discard_restart))
             }
          } else {
            Button(
              onClick = { onStartGeneration(bookId) },
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(stringResource(StringsR.string.generation_settings_action_start_generation))
            }
          }
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
          text = stringResource(StringsR.string.generation_settings_characters_voices),
          style = MaterialTheme.typography.titleMedium,
          modifier = Modifier.padding(16.dp),
        )
      }

      items(viewState.characters) { character ->
        val mapping = viewState.voiceMappings[character.id]
        CharacterVoiceItem(
          character = character,
          mapping = mapping,
          enabled = viewState.status != GenerationStatus.GENERATING,
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
      }
    }
  }
}

@Composable
private fun CharacterVoiceItem(
  character: Character,
  mapping: VoiceMapping?,
  enabled: Boolean,
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
      IconButton(
        onClick = { /* TODO: Edit voice settings */ },
        enabled = enabled,
      ) {
        Icon(imageVector = VoiceIcons.Settings, contentDescription = "Edit")
      }
    },
  )
}
