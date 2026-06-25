package voice.features.bookOverview.progress

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavEntry
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import voice.core.common.rootGraphAs
import voice.core.data.BookId
import voice.core.data.GenerationStatus
import voice.core.ui.icons.VoiceIcons
import voice.features.bookOverview.di.BookOverviewGraph
import voice.navigation.Destination
import voice.navigation.NavEntryProvider
import voice.core.strings.R as StringsR

@ContributesTo(AppScope::class)
interface ProgressTrackingProvider {

  @Provides
  @IntoSet
  fun progressTrackingNavEntryProvider(): NavEntryProvider<*> = NavEntryProvider<Destination.ProgressTracking> { key ->
    NavEntry(key) {
      ProgressTrackingScreen(bookId = key.bookId)
    }
  }
}

@Composable
private fun ProgressTrackingScreen(bookId: BookId) {
  val viewModel = retain<BookOverviewGraph> {
    rootGraphAs<BookOverviewGraph.Factory.Provider>()
      .bookOverviewGraphProviderFactory.create()
  }.progressTrackingViewModel
  ProgressTracking(
    bookId = bookId,
    viewState = viewModel.state(bookId),
    onConfigureGeneration = viewModel::onConfigureGeneration,
    onClose = viewModel::close,
  )
}

@Composable
private fun ProgressTracking(
  bookId: BookId,
  viewState: ProgressTrackingViewState,
  onConfigureGeneration: (BookId) -> Unit,
  onClose: () -> Unit,
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(stringResource(StringsR.string.library_progress_tracking_title)) },
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
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
      contentPadding = WindowInsets.systemBars.asPaddingValues(),
    ) {
      item {
        ListItem(
          headlineContent = {
            Text(stringResource(StringsR.string.library_progress_tracking_overall_status))
          },
          supportingContent = {
            Text(text = viewState.status.label())
          },
        )
      }
      if (viewState.status == GenerationStatus.FAILED && viewState.errorMessage != null) {
        item {
          ListItem(
            headlineContent = {
              Text(
                text = when (viewState.errorMessage) {
                  "Gemini API key is missing" -> stringResource(StringsR.string.library_progress_tracking_error_api_key_missing)
                  "Persistent API failure" -> stringResource(StringsR.string.library_progress_tracking_error_persistent_failure)
                  else -> viewState.errorMessage
                },
                color = androidx.compose.material3.MaterialTheme.colorScheme.error,
              )
            },
          )
        }
      }
      if (viewState.status == GenerationStatus.ANALYZED) {
        item {
          ListItem(
            headlineContent = {
              Button(
                onClick = { onConfigureGeneration(bookId) },
                modifier = Modifier.fillMaxWidth(),
              ) {
                Text(stringResource(StringsR.string.library_progress_tracking_action_configure_generation))
              }
            },
          )
        }
      }
      item {
        ListItem(
          headlineContent = {
            Text(stringResource(StringsR.string.library_progress_tracking_analysis_progress))
          },
          supportingContent = {
            Text(
              text = progressText(
                current = viewState.analysisCurrentChunk,
                total = viewState.analysisTotalChunks,
              ),
            )
          },
        )
      }
      item {
        ListItem(
          headlineContent = {
            Text(stringResource(StringsR.string.library_progress_tracking_generation_progress))
          },
          supportingContent = {
            val chapter = viewState.generationChapter
            val chunk = viewState.generationChunk
            val total = viewState.generationTotalChunks
            val text = if (chapter != null && chunk != null && total != null) {
              stringResource(
                StringsR.string.library_progress_tracking_generation_value,
                chapter,
                chunk,
                total,
              )
            } else {
              stringResource(StringsR.string.library_progress_tracking_not_started)
            }
            Text(text = text)
          },
        )
      }
      item {
        ListItem(
          headlineContent = {
            Text(stringResource(StringsR.string.library_progress_tracking_last_updated))
          },
          supportingContent = {
            val text = viewState.lastUpdated?.toString()
              ?: stringResource(StringsR.string.library_progress_tracking_not_started)
            Text(text = text)
          },
        )
      }
    }
  }
}

@Composable
private fun GenerationStatus?.label(): String {
  return when (this) {
    GenerationStatus.PENDING -> stringResource(StringsR.string.library_progress_tracking_status_pending)
    GenerationStatus.ANALYZING -> stringResource(StringsR.string.library_progress_tracking_status_analyzing)
    GenerationStatus.ANALYZED -> stringResource(StringsR.string.library_progress_tracking_status_analyzed)
    GenerationStatus.GENERATING -> stringResource(StringsR.string.library_progress_tracking_status_generating)
    GenerationStatus.COMPLETED -> stringResource(StringsR.string.library_progress_tracking_status_completed)
    GenerationStatus.FAILED -> stringResource(StringsR.string.library_progress_tracking_status_failed)
    null -> stringResource(StringsR.string.library_progress_tracking_not_started)
  }
}

@Composable
private fun progressText(
  current: Int?,
  total: Int?,
): String {
  return if (current != null && total != null) {
    stringResource(StringsR.string.library_progress_tracking_analysis_value, current, total)
  } else {
    stringResource(StringsR.string.library_progress_tracking_not_started)
  }
}
