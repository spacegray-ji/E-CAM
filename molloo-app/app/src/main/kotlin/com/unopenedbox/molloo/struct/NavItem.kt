package com.unopenedbox.molloo.struct

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(
  val icon:ImageVector,
  val label:String,
  val tabOnClick: () -> Unit = {},
  val content: @Composable (contentPadding:PaddingValues) -> Unit = {},
)