package com.unopenedbox.molloo.ui.compose.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PersonAddAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unopenedbox.molloo.struct.AppUser

@Composable
fun MainTopBar(
  user: AppUser = AppUser("Default"),
  onAddUserClicked: () -> Unit = {},
  onListUserClicked: () -> Unit = {},
) {
  CenterAlignedTopAppBar(
    title = { Text(text = user.name, fontSize = 18.sp, color = MaterialTheme.colorScheme.inverseOnSurface) },
    navigationIcon = {
      Icon(
        painter = painterResource(id = user.profileImage),
        contentDescription = "${user.name} Profile",
        modifier = Modifier
          .padding(start = 16.dp, end = 8.dp)
          .size(36.dp)
          .clickable {
            onListUserClicked()
          },
        tint = MaterialTheme.colorScheme.inverseOnSurface,
      )
    },
    actions = {
      /*
      Row(
        modifier = Modifier.padding(start=8.dp, end=8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = {
            onAddUserClicked()
          },
          content = {
            Icon(
              imageVector = Icons.Rounded.PersonAddAlt,
              contentDescription = "Manage Accounts",
              tint = MaterialTheme.colorScheme.inverseOnSurface,
              modifier = Modifier.size(32.dp),
            )
          },
        )
      }
       */
    },
    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
      containerColor = MaterialTheme.colorScheme.surfaceTint,
    )
  )
}

@Preview
@Composable
fun MainTopBarPreview() {
  MainTopBar()
}