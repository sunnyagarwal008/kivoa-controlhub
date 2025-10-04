package com.kivoa.controlhub.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kivoa.controlhub.api.RetrofitInstance
import com.kivoa.controlhub.data.Product
import com.kivoa.controlhub.data.ProductPagingSource
import kotlinx.coroutines.flow.Flow

class BrowseViewModel : ViewModel() {
    fun getProducts(category: String, excludeOutOfStock: Boolean): Flow<PagingData<Product>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = { ProductPagingSource(RetrofitInstance.api, category, excludeOutOfStock) }
        ).flow.cachedIn(viewModelScope)
    }
}
