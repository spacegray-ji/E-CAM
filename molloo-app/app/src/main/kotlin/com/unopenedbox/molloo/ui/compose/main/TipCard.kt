@file:OptIn(ExperimentalMaterial3Api::class)

package com.unopenedbox.molloo.ui.compose.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TipsAndUpdates
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
fun TipCard(tipText:String, modifier: Modifier = Modifier, onClick:() -> Unit = {}) {
  ElevatedCard(
    onClick = onClick,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = modifier
        .fillMaxWidth()
        .padding(all = 12.dp),
    ) {
      Icon(
        imageVector = Icons.Outlined.TipsAndUpdates,
        contentDescription = "Tip",
        modifier = Modifier.size(48.dp).padding(all = 8.dp),
        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
      )
      Spacer(Modifier.width(16.dp))
      Text(
        text = tipText,
        style = MaterialTheme.typography.bodyMedium,
      )
    }
  }
}

@Preview(showBackground = false)
@Composable
fun TipCardPreview() {
  TipCard("Tip is a tip.")
}