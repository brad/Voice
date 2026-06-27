package voice.features.bookOverview.progress

import android.content.ClipData
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.launch
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
  val scope = rememberCoroutineScope()
  ProgressTracking(
    bookId = bookId,
    viewState = viewModel.state(bookId),
    onConfigureGeneration = viewModel::onConfigureGeneration,
    onRetry = { viewModel.onRetry(bookId, scope) },
    onCancel = { viewModel.onCancel(bookId, scope) },
    onShareError = viewModel::shareError,
    onClose = viewModel::close,
  )
}

@Composable
private fun ProgressTracking(
  bookId: BookId,
  viewState: ProgressTrackingViewState,
  onConfigureGeneration: (BookId) -> Unit,
  onRetry: () -> Unit,
  onCancel: () -> Unit,
  onShareError: (String) -> android.net.Uri,
  onClose: () -> Unit,
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()
  val clipboard = LocalClipboard.current
  val context = LocalContext.current
  var showErrorDetails by remember { mutableStateOf(false) }
  val copiedMessage = stringResource(StringsR.string.library_progress_tracking_error_details_copied)

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(stringResource(StringsR.string.library_progress_tracking_title)) },
        actions = {
          val isRunning =
            viewState.status == GenerationStatus.ANALYZING ||
              viewState.status == GenerationStatus.GENERATING ||
              viewState.status == GenerationStatus.PENDING
          if (isRunning) {
            TextButton(onClick = onCancel) {
              Text(stringResource(StringsR.string.library_progress_tracking_action_cancel))
            }
          }
        },
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
    snackbarHost = { SnackbarHost(snackbarHostState) },
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
            val isRateLimited = viewState.retryAfter != null && viewState.retryAfter.isAfter(java.time.Instant.now())
            val label = if (isRateLimited) {
              stringResource(StringsR.string.library_progress_tracking_status_rate_limited)
            } else {
              viewState.status.label()
            }
            Text(text = label)
          },
        )
      }
      if (viewState.status == GenerationStatus.FAILED && viewState.errorMessage != null) {
        item {
          ListItem(
            headlineContent = {
              Column {
                Text(
                  text = stringResource(StringsR.string.library_progress_tracking_error_summary_label),
                  style = MaterialTheme.typography.labelMedium,
                )
                Text(
                  text = viewState.errorMessage.lineSequence().first(),
                  color = MaterialTheme.colorScheme.error,
                  maxLines = 2,
                )
                TextButton(
                  onClick = { showErrorDetails = true },
                  modifier = Modifier.align(Alignment.End),
                ) {
                  Text(stringResource(StringsR.string.common_action_more))
                }
              }
            },
          )
        }
        item {
          ListItem(
            headlineContent = {
              Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(),
              ) {
                Text(stringResource(StringsR.string.library_progress_tracking_action_retry))
              }
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

  if (showErrorDetails && viewState.errorMessage != null) {
    AlertDialog(
      onDismissRequest = { showErrorDetails = false },
      title = { Text(stringResource(StringsR.string.library_progress_tracking_error_title)) },
      text = {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
          Text(
            text = viewState.errorMessage,
            style = MaterialTheme.typography.bodySmall,
          )
        }
      },
      confirmButton = {
        TextButton(onClick = { showErrorDetails = false }) {
          Text(stringResource(StringsR.string.common_dialog_ok))
        }
      },
      dismissButton = {
        Row {
          IconButton(
            onClick = {
              scope.launch {
                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Error", viewState.errorMessage)))
                snackbarHostState.showSnackbar(copiedMessage)
              }
            },
          ) {
            Icon(
              imageVector = VoiceIcons.ContentCopy,
              contentDescription = stringResource(StringsR.string.library_progress_tracking_action_copy_error),
            )
          }
          IconButton(
            onClick = {
              val uri = onShareError(viewState.errorMessage)
              val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
              }
              context.startActivity(Intent.createChooser(intent, null))
            },
          ) {
            Icon(
              imageVector = VoiceIcons.Share,
              contentDescription = stringResource(StringsR.string.library_progress_tracking_action_share_error),
            )
          }
        }
      },
    )
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
