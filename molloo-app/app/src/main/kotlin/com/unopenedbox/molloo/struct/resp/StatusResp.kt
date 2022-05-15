package com.unopenedbox.molloo.struct.resp

import kotlinx.serialization.Serializable

@Serializable
data class StatusResp(val alive:Boolean, val main:String, val app:String)
