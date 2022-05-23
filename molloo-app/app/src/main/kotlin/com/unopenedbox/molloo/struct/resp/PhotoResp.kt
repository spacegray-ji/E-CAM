package com.unopenedbox.molloo.struct.resp

import com.unopenedbox.molloo.struct.PhotoInfo
import kotlinx.serialization.Serializable

@Serializable
data class PhotoResp(val dirpath:String, val photos:List<PhotoInfo>)