@file:OptIn(ExperimentalMaterial3Api::class)

package com.unopenedbox.molloo.ui.compose.setting

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.skydoves.landscapist.glide.GlideImage
import com.unopenedbox.molloo.BuildConfig
import com.unopenedbox.molloo.R
import com.unopenedbox.molloo.struct.PhotoInfo
import kotlinx.datetime.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


@Composable
fun UserInfoCard(userInfo:String, modifier: Modifier = Modifier, onClick:(clickType:UserInfoClickType) -> Unit = {}) {
  ElevatedCard(
    modifier = modifier.padding(vertical = 16.dp),
    onClick = { onClick(UserInfoClickType.CARD) },
  ) {
    Row (
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(all = 20.dp),
    ) {
      Icon(
        painter = painterResource(id = R.drawable.ic_user),
        contentDescription = "User",
        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        modifier = Modifier.size(80.dp),
      )
      Spacer(modifier = Modifier.width(20.dp))
      Column(modifier = Modifier
        .weight(1f)) {
        Text(userInfo, style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(4.dp))
        Text(stringResource(id = R.string.card_user_change_guide),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          horizontalArrangement = Arrangement.End,
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedButton(onClick = { onClick(UserInfoClickType.CREATE) }) {
            Text(
              text = stringResource(id = R.string.add),
              style = MaterialTheme.typography.bodyMedium,
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          OutlinedButton(onClick = { onClick(UserInfoClickType.SWITCH) }) {
            Text(
              text = stringResource(id = R.string.switch_user),
              style = MaterialTheme.typography.bodyMedium,
            )
          }
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun UserInfoCardPreview() {
  UserInfoCard(
    userInfo = "Default",
  )
}

enum class UserInfoClickType {
  CREATE,
  REMOVE,
  SWITCH,
  CARD,
}