package com.molloo.resp

import com.molloo.structure.PhotoInfo
import kotlinx.serialization.Serializable

@Serializable
data class PhotoResBody(val dirpath:String, val photos:Array<PhotoInfo>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PhotoResBody

        if (dirpath != other.dirpath) return false
        if (!photos.contentEquals(other.photos)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dirpath.hashCode()
        result = 31 * result + photos.contentHashCode()
        return result
    }
}
