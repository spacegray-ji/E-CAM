package com.unopenedbox.molloo.ui.compose.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.unopenedbox.molloo.R
import com.unopenedbox.molloo.ui.compose.care.IconSelection
import kotlin.time.Duration

@Composable
fun UserListEditDialog(isVisible:Boolean, userList:List<String>, currentUser:String, onRemoveUser:(username:String) -> Unit = {}, onSwitchUser:(username:String) -> Unit = {}, onAddUser:() -> Unit = {}, onDismiss: () -> Unit = {}) {
  if (isVisible) {
    AlertDialog(
      onDismissRequest = {
        onDismiss()
      },
      title = {
        Text(stringResource(id = R.string.dialog_sel_user_title))
      },
      text = {
        val replDefault = stringResource(id = R.string.username_default)
        LazyColumn(
          modifier = Modifier.fillMaxWidth(),
        ) {
          items(
            count = userList.size,
            key = { index -> userList[index]},
          ) { itemIndex ->
            val item = userList[itemIndex].let { name ->
              if (name == "Default") {
                replDefault
              } else {
                name
              }
            }
            val tintStyle = if (userList[itemIndex] == currentUser) {
              MaterialTheme.colorScheme.surfaceTint
            } else {
              MaterialTheme.colorScheme.onSurface
            }.copy(alpha = 0.85f)
            Box(
              modifier = Modifier.fillMaxWidth().clickable {
                onSwitchUser(userList[itemIndex])
              },
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
              ) {
                Icon(
                  imageVector = Icons.Outlined.AssignmentInd,
                  contentDescription = "User",
                  tint = tintStyle,
                  modifier = Modifier.size(32.dp),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                  text = item,
                  style = MaterialTheme.typography.bodyMedium,
                  modifier = Modifier.weight(1f),
                  color = tintStyle,
                )
                if (item != replDefault) {
                  IconButton(onClick = {
                    onRemoveUser(userList[itemIndex])
                  }) {
                    Icon(
                      imageVector = Icons.Outlined.Delete,
                      contentDescription = "Delete",
                      tint = tintStyle,
                      modifier = Modifier.size(28.dp),
                    )
                  }
                } else {
                  IconButton(onClick = {}) {
                    Icon(
                      imageVector = Icons.Outlined.DisabledByDefault,
                      contentDescription = "Delete (disabled)",
                      tint = tintStyle,
                      modifier = Modifier.size(28.dp),
                    )
                  }
                }
              }
            }
          }
        }
      },
      icon = {
        Icon(
          imageVector = Icons.Outlined.AccountCircle,
          contentDescription = "Account",
        )
      },
      dismissButton = {
        OutlinedButton(onClick = { onDismiss() }) {
          Text(stringResource(id = R.string.cancel))
        }
      },
      confirmButton = {
        OutlinedButton(onClick = { onAddUser() }) {
          Text(stringResource(id = R.string.add))
        }
      }
    )
  }
}

@Preview
@Composable
fun UserListEditDialogPreview() {
  UserListEditDialog(isVisible = true, userList = listOf("User1", "User2", "User3", "User4"), currentUser = "User1")
}