package com.kivoa.controlhub.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.kivoa.controlhub.api.ApiService

class ProductPagingSource(
    private val apiService: ApiService,
    private val category: String,
    private val excludeOutOfStock: Boolean,
    private val minPrice: Int?,
    private val maxPrice: Int?
) : PagingSource<Int, ApiProduct>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ApiProduct> {
        val page = params.key ?: 1
        return try {
            val response = apiService.getProducts(category = category, page = page, excludeOutOfStock = excludeOutOfStock, minPrice = minPrice, maxPrice = maxPrice)
            val products = response.data
            LoadResult.Page(
                data = products,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.pagination.total > page) page + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ApiProduct>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}