package com.unopenedbox.molloo.struct

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PhotoInfo(
  val id: String,
  val filename: String,
  val createdAt:Instant,
  val pixelSize:Int = 0,
  val cavityLevel:Int = -1,
  var imageURL:String = "",
)