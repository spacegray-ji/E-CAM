@file:OptIn(ExperimentalMaterial3Api::class)

package com.unopenedbox.molloo.ui.compose.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unopenedbox.molloo.R


@Composable
fun CamInfoCard(camSerial:String, modifier: Modifier = Modifier, onClick:() -> Unit = {}) {
  OutlinedCard(
    onClick = onClick,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = modifier
        .fillMaxWidth()
        .padding(all = 20.dp),
    ) {
      Image(
        painter = painterResource(id = R.drawable.ic_webcam),
        contentDescription = "Camera Icon",
        modifier = Modifier.size(80.dp)
      )
      Column(
        modifier = Modifier.padding(start = 20.dp),
      ) {
        Text(
          text = stringResource(id = R.string.card_ecam_connected),
          style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = stringResource(id = R.string.card_ecam_serial).format(camSerial),
          style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = stringResource(id = R.string.card_ecam_suggestcapture),
          style = MaterialTheme.typography.bodyMedium,
        )
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun InfoCamCardPreview() {
  CamInfoCard(
    "CAMSERIAL0000"
  ) {

  }
}