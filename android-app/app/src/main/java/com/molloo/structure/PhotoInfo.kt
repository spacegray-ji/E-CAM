package com.molloo.structure

import kotlinx.serialization.Serializable

@Serializable
data class PhotoInfo(val id:String, val filename:String, val pixelSize: Int)
