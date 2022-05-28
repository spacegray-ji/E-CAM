package com.unopenedbox.molloo.struct.resp

import kotlinx.serialization.Serializable

@Serializable
data class VerifyTokenResp(val valid:Boolean, val type: String, val serial: String, val username: String)