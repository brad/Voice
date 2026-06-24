package voice.features.settings.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import voice.core.strings.R as StringsR

@Composable
internal fun AudiobookGenerationDialog(
  initialApiKey: String,
  initialAnalysisModel: String,
  initialGenerationModel: String,
  availableModels: List<String>,
  onConfirm: (apiKey: String, analysisModel: String, generationModel: String) -> Unit,
  onDismiss: () -> Unit,
) {
  var apiKey by remember(initialApiKey) { mutableStateOf(initialApiKey) }
  var analysisModel by remember(initialAnalysisModel) { mutableStateOf(initialAnalysisModel) }
  var generationModel by remember(initialGenerationModel) { mutableStateOf(initialGenerationModel) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(text = stringResource(StringsR.string.settings_audiobook_generation_title))
    },
    text = {
      Column {
        OutlinedTextField(
          value = apiKey,
          onValueChange = { apiKey = it },
          label = { Text(stringResource(StringsR.string.settings_gemini_api_key_title)) },
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
          singleLine = true,
        )

        ModelSelector(
          label = stringResource(StringsR.string.settings_gemini_analysis_model_title),
          selectedModel = analysisModel,
          availableModels = availableModels,
          onModelSelect = { analysisModel = it },
        )

        ModelSelector(
          label = stringResource(StringsR.string.settings_gemini_generation_model_title),
          selectedModel = generationModel,
          availableModels = availableModels,
          onModelSelect = { generationModel = it },
          modifier = Modifier.padding(top = 16.dp),
        )
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          onConfirm(apiKey, analysisModel, generationModel)
        },
      ) {
        Text(stringResource(StringsR.string.common_dialog_confirm))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(StringsR.string.common_dialog_cancel))
      }
    },
  )
}

@Composable
private fun ModelSelector(
  label: String,
  selectedModel: String,
  availableModels: List<String>,
  onModelSelect: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }

  if (availableModels.isEmpty()) {
    OutlinedTextField(
      value = selectedModel,
      onValueChange = onModelSelect,
      label = { Text(label) },
      modifier = modifier.fillMaxWidth(),
      singleLine = true,
    )
  } else {
    Box(modifier = modifier) {
      OutlinedTextField(
        value = selectedModel,
        onValueChange = {},
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        trailingIcon = {
          Box(
            modifier = Modifier
              .clickable { expanded = true }
              .padding(8.dp),
          ) {
            // Dropdown icon could be added here
          }
        },
      )
      // Workaround for OutlinedTextField not being clickable
      Box(
        modifier = Modifier
          .matchParentSize()
          .clickable { expanded = true },
      )
      DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
      ) {
        availableModels.forEach { model ->
          DropdownMenuItem(
            text = { Text(model) },
            onClick = {
              onModelSelect(model)
              expanded = false
            },
          )
        }
      }
    }
  }
}
