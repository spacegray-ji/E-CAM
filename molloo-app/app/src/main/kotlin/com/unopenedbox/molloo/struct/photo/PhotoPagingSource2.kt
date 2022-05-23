package com.unopenedbox.molloo.struct.photo

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.unopenedbox.molloo.network.MollooRequest
import com.unopenedbox.molloo.struct.PhotoInfo
import com.unopenedbox.molloo.struct.photo.PhotoListIndex

class PhotoPagingSource2(
  private val request: MollooRequest,
  private val token: String,
) : PagingSource<Int, PhotoInfo>() {
  override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PhotoInfo> {
    val page = params.key ?: 1
    return try {
      val items = request.fetchPhotos(
        token,
        maxCount = params.loadSize,
        page = page,
      )
      LoadResult.Page(
        data = items,
        prevKey = if (page == 1) null else page - 1,
        nextKey = if (items.isEmpty()) null else page + 1,
      )
    } catch (e: Exception) {
      return LoadResult.Error(e)
    }
  }

  override fun getRefreshKey(state: PagingState<Int, PhotoInfo>): Int? {
    return state.anchorPosition?.let { anchorPosition ->
      state.closestPageToPosition(anchorPosition)?.let { position ->
        position.prevKey?.plus(1) ?: position.nextKey?.minus(1)
      }
    }
  }
}