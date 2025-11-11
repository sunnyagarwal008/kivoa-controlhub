package com.kivoa.controlhub.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.kivoa.controlhub.api.ApiService

class RawImagePagingSource(
    private val apiService: ApiService
) : PagingSource<Int, ApiRawImage>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ApiRawImage> {
        val page = params.key ?: 1
        return try {
            val response = apiService.getRawImages(page = page, perPage = params.loadSize)
            LoadResult.Page(
                data = response.data,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.pagination.pages <= page) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ApiRawImage>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}