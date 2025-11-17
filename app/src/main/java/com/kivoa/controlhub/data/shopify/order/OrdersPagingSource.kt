package com.kivoa.controlhub.data.shopify.order

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.kivoa.controlhub.api.ApiService

class OrdersPagingSource(
    private val apiService: ApiService,
    private val status: String,
    private val financialStatus: String?,
    private val fulfillmentStatus: String?
) : PagingSource<String, Order>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Order> {
        return try {
            val pageInfo = params.key
            val response = apiService.getOrders(
                pageInfo = pageInfo,
                limit = params.loadSize,
            )
            val orders = response.data.orders
            LoadResult.Page(
                data = orders,
                prevKey = if (response.data.pagination.hasPrevious) response.data.pagination.previousPageInfo else null,
                nextKey = if (response.data.pagination.hasNext) response.data.pagination.nextPageInfo else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, Order>): String? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey
                ?: state.closestPageToPosition(anchorPosition)?.nextKey
        }
    }
}
