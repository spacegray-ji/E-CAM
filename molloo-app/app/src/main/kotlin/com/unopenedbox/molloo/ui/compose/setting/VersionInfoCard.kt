@file:OptIn(ExperimentalMaterial3Api::class)
package com.unopenedbox.molloo.ui.compose.setting

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.unopenedbox.molloo.BuildConfig
import com.unopenedbox.molloo.R

@Composable
fun VersionInfoCard(modifier: Modifier = Modifier, onClick:() -> Unit = {}) {
  Card(
    onClick = onClick,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = modifier
        .fillMaxWidth()
        .padding(all = 20.dp),
    ) {
      Image(
        painter = painterResource(id = R.drawable.ic_launcher_foreground),
        contentDescription = "Launcher Icon",
        modifier = Modifier.size(80.dp),
      )
      Column(
        modifier = Modifier.padding(start = 20.dp),
      ) {
        Text(
          text = stringResource(id = R.string.card_info_title).format(BuildConfig.VERSION_NAME),
          style = MaterialTheme.typography.titleLarge,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = stringResource(id = R.string.card_info_description),
            style = MaterialTheme.typography.bodyMedium,
          )
          Spacer(modifier = Modifier.weight(1f))
          Icon(
            painter = painterResource(id = R.drawable.ic_github),
            contentDescription = "Github Icon",
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface,
          )
        }
      }
    }
  }
}

@Preview(showBackground = false)
@Composable
fun VersionInfoCardPreview() {
  VersionInfoCard()
}