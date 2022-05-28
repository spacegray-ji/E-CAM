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
fun CaptureButton(onDeviceClick: () -> Unit, onRemoteClick: () -> Unit, modifier: Modifier = Modifier) {
  Card {
    Row(
      modifier = modifier
        .fillMaxWidth()
        .padding(all = 20.dp),
    ) {
      Column {
        Text(
          modifier = Modifier.fillMaxWidth(),
          text = stringResource(id = R.string.capture_suggest_title),
          style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        ) {
          Column(
            modifier = Modifier
              .weight(1f,)
              .clickable { onRemoteClick() }
              .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            ImageTextButton(
              iconRes = R.drawable.ic_webcam,
              textRes = R.string.capture_device_cam,
            )
          }
          Divider(
            modifier = Modifier
              .padding(vertical = 10.dp)
              .width(1.dp)
              .fillMaxHeight(),
          )
          Column(
            modifier = Modifier
              .weight(1f,)
              .clickable { onDeviceClick() }
              .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            ImageTextButton(
              iconRes = R.drawable.ic_phone_camera,
              textRes = R.string.capture_device_phone,
            )
          }
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun CapturePreview() {
  CaptureButton(
    onDeviceClick = {},
    onRemoteClick = {},
  )
}

@Composable
fun ImageTextButton(
  @DrawableRes iconRes: Int,
  @StringRes textRes: Int,
  modifier: Modifier = Modifier
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier,
  ) {
    Image(
      painter = painterResource(id = iconRes),
      contentDescription = "Button",
      modifier = Modifier.height(100.dp)
    )
    Spacer(modifier = Modifier.height(10.dp))
    Text(
      text = stringResource(id = textRes),
      textAlign = TextAlign.Center,
      style = MaterialTheme.typography.labelLarge,
    )
  }
}