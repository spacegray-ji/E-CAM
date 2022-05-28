package com.unopenedbox.molloo.struct

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class DentalHistory(
  val id: Long,
  val reason:String,
  val careDate: Instant,
  val nextCareDate: Instant,
)