package com.unopenedbox.molloo.struct

import androidx.annotation.DrawableRes
import com.unopenedbox.molloo.R

data class AppUser(
  val name:String,
  @DrawableRes val profileImage:Int = R.drawable.ic_user,
)
