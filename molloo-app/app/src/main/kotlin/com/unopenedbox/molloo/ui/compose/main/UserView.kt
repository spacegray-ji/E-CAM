package com.unopenedbox.molloo.ui.compose.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.unopenedbox.molloo.R
import com.unopenedbox.molloo.struct.AppUser

@Composable
fun UserView(
  user: AppUser,
  onClick: () -> Unit,
) {
  val image = painterResource(id = user.profileImage)
  val name = user.name

  Row(
    modifier = Modifier.padding(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Image(
      painter = image,
      contentDescription = "Profile image",
      modifier = Modifier.size(96.dp),
    )
    Column(
      modifier = Modifier.padding(start = 20.dp),
    ) {
      Text(
        text = name,
        style = MaterialTheme.typography.titleLarge,
      )
    }
  }
}

@Preview
@Composable
fun UserViewPreview() {
  UserView(
    user = AppUser(
      name = "Default",
      profileImage = R.drawable.ic_account,
    ),
    onClick = {},
  )
}

