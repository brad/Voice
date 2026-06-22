package voice.features.settings.views

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import voice.core.strings.R as StringsR

@Composable
fun StringSettingDialog(
  title: String,
  initialValue: String,
  onConfirm: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  var value by remember(initialValue) { mutableStateOf(initialValue) }
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(text = title)
    },
    text = {
      OutlinedTextField(
        value = value,
        onValueChange = {
          value = it
        },
        singleLine = true,
      )
    },
    confirmButton = {
      TextButton(
        onClick = {
          onConfirm(value)
          onDismiss()
        },
      ) {
        Text(stringResource(StringsR.string.common_dialog_confirm))
      }
    },
    dismissButton = {
      TextButton(
        onClick = {
          onDismiss()
        },
      ) {
        Text(stringResource(StringsR.string.common_dialog_cancel))
      }
    },
  )
}
