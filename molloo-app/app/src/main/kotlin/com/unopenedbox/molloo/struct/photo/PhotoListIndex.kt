package com.unopenedbox.molloo.struct.photo

data class PhotoListIndex(
  val isPast: Boolean,
  val includeTarget: Boolean,
  val targetId: String,
)