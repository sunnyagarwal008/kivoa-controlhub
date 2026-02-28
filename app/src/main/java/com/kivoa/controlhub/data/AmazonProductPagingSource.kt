package com.kivoa.controlhub.data

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.kivoa.controlhub.api.ApiService

class AmazonProductPagingSource(
    private val apiService: ApiService,
) : PagingSource<Int, AmazonProduct>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, AmazonProduct> {
        val page = params.key ?: 1
        return try {
            val response = apiService.getChannelListings(
                channelName = "amazon",
                page = page,
            )
            Log.d("AmazonProductPagingSource", "Response: $response")
            val products = response.data.filter { it.productId != null }
            LoadResult.Page(
                data = products,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.pagination.pages > page) page + 1 else null
            )
        } catch (e: Exception) {
            Log.e("AmazonProductPagingSource", "Error loading amazon products", e)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, AmazonProduct>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}