package com.unopenedbox.molloo.struct.photo

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.unopenedbox.molloo.network.MollooRequest
import com.unopenedbox.molloo.struct.PhotoInfo
import com.unopenedbox.molloo.struct.photo.PhotoListIndex

class PhotoPagingSource(
  private val request: MollooRequest,
  private val token: String,
): PagingSource<PhotoListIndex, PhotoInfo>() {
  override suspend fun load(params: LoadParams<PhotoListIndex>): LoadResult<PhotoListIndex, PhotoInfo> {
    val isPast = params.key?.isPast ?: true
    val targetId = params.key?.targetId ?: ""
    val includeTarget = params.key?.includeTarget ?: false
    return try {
      val items = request.fetchPhotos(token,
        maxCount = params.loadSize,
        beforeId = if (isPast) targetId else "",
        afterId = if (isPast) "" else targetId,
      )
      LoadResult.Page(
        data = items,
        prevKey = if (items.isEmpty()) null else PhotoListIndex(
          isPast = false,
          includeTarget = false,
          targetId = items.first().id
        ),
        nextKey = if (items.isEmpty()) null else PhotoListIndex(
          isPast = true,
          includeTarget = false,
          targetId = items.last().id
        ),
      )
    } catch (e:Exception) {
      return LoadResult.Error(e)
    }
  }

  override fun getRefreshKey(state: PagingState<PhotoListIndex, PhotoInfo>): PhotoListIndex? {
    return state.anchorPosition?.let { anchorPosition ->
      state.closestPageToPosition(anchorPosition)?.let { position ->
        position.prevKey?.let { prevKey ->
          PhotoListIndex(
            isPast = !prevKey.isPast,
            includeTarget = true,
            targetId = prevKey.targetId,
          )
        } ?: position.nextKey?.let { nextKey ->
          PhotoListIndex(
            isPast = !nextKey.isPast,
            includeTarget = true,
            targetId = nextKey.targetId,
          )
        }
      }
    }
  }
}