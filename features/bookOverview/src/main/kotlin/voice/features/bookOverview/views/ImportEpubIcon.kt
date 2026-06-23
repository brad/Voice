package voice.features.bookOverview.views

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import voice.core.ui.icons.VoiceIcons
import voice.core.strings.R as StringsR

@Composable
internal fun ImportEpubIcon(onClick: () -> Unit) {
  IconButton(onClick = onClick) {
    Icon(
      imageVector = VoiceIcons.Download,
      contentDescription = stringResource(StringsR.string.library_import_epub),
    )
  }
}
