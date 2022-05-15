package com.unopenedbox.molloo.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.unopenedbox.molloo.R

@Composable
fun TextInputDialog(
  visible: Boolean,
  title:String,
  inputValue: String,
  onDismissRequest: () -> Unit,
  onConfirmRequest: () -> Unit,
  onInput: (String) -> Unit,
  desc: String = "",
  hint: String = "",
  positiveBtnText: String = stringResource(id = R.string.ok),
) {
  if (visible) {
    CustomAlertDialog(onDismissRequest = { onDismissRequest() }) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.align(Alignment.Start)
        )
        if (desc.isNotEmpty()) {
          Text(
            text = desc,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.Start)
          )
        }
        OutlinedTextField(
          value = inputValue,
          onValueChange = onInput,
          placeholder = {
            Text(text = hint)
          },
        )
        Row(
          modifier = Modifier
            .padding(start = 8.dp, end = 8.dp)
            .fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedButton(
            onClick = {
              onDismissRequest()
            },
            content = { Text(stringResource(id = R.string.cancel)) },
          )
          Spacer(
            Modifier
              .width(10.dp)
              .height(IntrinsicSize.Max))
          TextButton(
            onClick = {
              onConfirmRequest()
            },
            content = { Text(positiveBtnText) },
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.surfaceTint,
              contentColor = MaterialTheme.colorScheme.inverseOnSurface,
            )
          )
        }
      }
    }
  }
}

@Composable
private fun CustomAlertDialog(
  onDismissRequest: () -> Unit,
  properties: DialogProperties = DialogProperties(),
  content: @Composable () -> Unit,
) {
  Dialog(
    onDismissRequest = onDismissRequest,
    properties = properties
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight(),
      shape = RoundedCornerShape(12.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 8.dp
    ) {
      content()
    }
  }
}

@Composable
fun DefaultDialog() {
  AlertDialog(
    onDismissRequest = { /*TODO*/ },
    title = {
      Text(text = "Dialog Title")
    },
    text = {
      Text("Here is a text ")
    },
    confirmButton = {
      Button(onClick = {}) {
        Text("This is the Confirm Button")
      }
    },
  )
}

@Preview
@Composable
fun TextInputDialogPreview() {
  val text = remember { mutableStateOf("Text") }
  TextInputDialog(
    visible = true,
    title = "Hello",
    inputValue = text.value,
    onDismissRequest = {
      // uiModel.setCamAddDialogInput("")
      // uiModel.setShowCamAddDialog(false)
    },
    onConfirmRequest = {
      // uiModel.setShowCamAddDialog(false)
    },
    onInput = { text ->
      // val search = Regex("^[A-Za-z0-9]+").find(text)
      // uiModel.setCamAddDialogInput(search?.value ?: "")
    },
    desc = "Description",
    hint = "This is hint!",
  )
}