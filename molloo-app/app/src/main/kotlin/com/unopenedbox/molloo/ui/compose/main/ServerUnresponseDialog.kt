package com.unopenedbox.molloo.ui.compose.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.unopenedbox.molloo.R

@Composable
fun ServerUnresponseDialog(isVisible:Boolean, onDismissRequest: () -> Unit = {}) {
  if (isVisible) {
    AlertDialog(
      onDismissRequest = {},
      title = {
        Text(stringResource(id = R.string.dialog_noresp_title))
      },
      text = {
        Text(stringResource(id = R.string.dialog_noresp_message))
      },
      icon = {
        Icon(
          imageVector = Icons.Outlined.ErrorOutline,
          contentDescription = "Error",
        )
      },
      confirmButton = {
        OutlinedButton(onClick = { onDismissRequest() }) {
          Text(stringResource(id = R.string.ok))
        }
      }
    )
  }
}

@Preview
@Composable
fun ServerUnresponseDialogPreview() {
  ServerUnresponseDialog(isVisible = true, onDismissRequest = {})
}