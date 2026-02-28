package com.kivoa.controlhub.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.kivoa.controlhub.api.ApiService
import kotlinx.coroutines.flow.Flow

class AmazonProductRepository(private val apiService: ApiService) {

    fun getAmazonProducts(): Flow<PagingData<AmazonProduct>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { AmazonProductPagingSource(apiService) }
        ).flow
    }
}