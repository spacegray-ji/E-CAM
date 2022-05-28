@file:OptIn(ExperimentalMaterial3Api::class)
package com.unopenedbox.molloo.ui.compose.care

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
import com.unopenedbox.molloo.struct.DentalHistory
import kotlinx.datetime.Clock
import kotlinx.datetime.isDistantPast
import kotlinx.datetime.toJavaInstant
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.Duration.Companion.days


@Composable
fun DentalInfoCard(
  dentalHistory: DentalHistory,
  modifier: Modifier = Modifier,
  onClick:() -> Unit = {}
) {
  Card(
    onClick = onClick,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = modifier
        .fillMaxWidth()
        .padding(all = 20.dp),
    ) {
      Icon(
        painter = painterResource(id = R.drawable.ic_checklist),
        contentDescription = "Dental Icon",
        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.87f),
        modifier = Modifier.size(80.dp),
      )
      Column(
        modifier = Modifier.padding(start = 20.dp),
      ) {
        Text(
          text = dentalHistory.reason,
          style = MaterialTheme.typography.titleLarge,
        )
        Spacer(modifier = Modifier.height(12.dp))
        val careDateText = SimpleDateFormat(stringResource(id = R.string.format_yyyymmdd), Locale.getDefault()).format(Date.from(dentalHistory.careDate.toJavaInstant()))
        DateItemColumn(
          iconRes = R.drawable.ic_calendar,
          text = stringResource(id = R.string.care_date_format).format(careDateText),
        )
        Spacer(modifier = Modifier.height(8.dp))
        val nextCareDateText = SimpleDateFormat(stringResource(id = R.string.format_yyyymmdd), Locale.getDefault()).format(Date.from(dentalHistory.nextCareDate.toJavaInstant()))
        DateItemColumn(
          iconRes = R.drawable.ic_fast_forward,
          text = stringResource(id = R.string.care_next_date_format).format(
            if (dentalHistory.nextCareDate.isDistantPast) {
              stringResource(id = R.string.care_feature_none)
            } else {
              nextCareDateText
            }
          ),
        )
      }
    }
  }
}

@Composable
fun DateItemColumn(@DrawableRes iconRes: Int, text: String) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      painter = painterResource(id = iconRes),
      contentDescription = "Calendar Icon",
      tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.87f),
      modifier = Modifier.size(20.dp),
    )
    Spacer(modifier = Modifier.width(10.dp))
    Text(
      text = text,
      style = MaterialTheme.typography.bodyMedium,
    )
  }
}

@Preview(showBackground = false)
@Composable
fun DentalInfoCardPreview() {
  DentalInfoCard(
    DentalHistory(
      id = 0,
      reason = "Scaling",
      careDate = Clock.System.now(),
      nextCareDate = Clock.System.now().plus(180.days),
    )
  )
}