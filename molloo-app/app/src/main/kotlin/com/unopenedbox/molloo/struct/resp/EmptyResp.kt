package com.unopenedbox.molloo.struct.resp

import kotlinx.serialization.Serializable

@Serializable
data class EmptyResp(val dummy:Byte = 0)