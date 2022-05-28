@file:OptIn(ExperimentalMaterial3Api::class)

package com.unopenedbox.molloo.ui.compose.history

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
fun PhotoInfoCard(photoInfo: PhotoInfo, modifier: Modifier = Modifier, cardIndex:Int = -1, isPreview:Boolean = false, onClick:(clickType:ClickType) -> Unit = {}) {
  Card(
    modifier = modifier.padding(vertical = 16.dp),
    onClick = { onClick(ClickType.CARD) },
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .padding(all = 20.dp),
    ) {
      if (isPreview) {
        Icon(
          imageVector = Icons.Outlined.Image,
          contentDescription = "Preview",
          tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
          modifier = Modifier
            .width(100.dp)
            .aspectRatio(1.75f)
            .background(MaterialTheme.colorScheme.surfaceTint),
        )
      } else {
        GlideImage(
          imageModel = photoInfo.imageURL,
          contentScale = ContentScale.Crop,
          placeHolder = Icons.Outlined.Image,
          modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.75f)
            .clip(shape = RoundedCornerShape(12.dp)),
        )
      }
      Spacer(modifier = Modifier.height(16.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
      ) {
        Image(
          painter = painterResource(id = listOf(R.drawable.ic_safe, R.drawable.ic_warning, R.drawable.ic_error).getOrElse(
            photoInfo.cavityLevel
          ) { R.drawable.ic_error }),
          contentDescription = "Cavity Level",
          modifier = Modifier.size(36.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
          text = stringResource(id = R.string.card_photo_state).format(
            stringResource(id = listOf(R.string.cavity_state_0, R.string.cavity_state_1, R.string.cavity_state_2).getOrElse(
              photoInfo.cavityLevel
            ) { R.string.cavity_state_unknown })
          ),
          style = MaterialTheme.typography.titleLarge,
          modifier = Modifier.align(Alignment.CenterVertically)
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
      ) {
        val localCreatedAt = photoInfo.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime()
        Column(
          modifier = Modifier.align(Alignment.CenterVertically),
        ) {
          if (cardIndex >= 0 && BuildConfig.DEBUG) {
            Text(
              text = cardIndex.toString(),
              style = MaterialTheme.typography.bodyMedium,
            )
          }
          Text(
            text = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(localCreatedAt),
            style = MaterialTheme.typography.bodyMedium,
          )
        }
        Spacer(Modifier.weight(1f))
        IconButton(
          onClick = { onClick(ClickType.REMOVE) }
        ) {
          Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = "Remove",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.size(36.dp)
          )
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun PhotoInfoCardPreview() {
  PhotoInfoCard(
    PhotoInfo(
      id = "00001",
      filename = "test.jpg",
      createdAt = Clock.System.now(),
      cavityLevel = 0,
    ),
    cardIndex = 100,
    modifier = Modifier,
    isPreview = true,
  )
}

enum class ClickType {
  CARD,
  REMOVE,
}