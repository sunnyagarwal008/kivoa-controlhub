package com.kivoa.controlhub.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

    var selectedCategory by mutableStateOf("All products")
    var excludeOutOfStock by mutableStateOf(true)
    var priceRange by mutableStateOf(100f..4000f)

    fun getProducts(): Flow<PagingData<Product>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = { ProductPagingSource(
                apiService = RetrofitInstance.api,
                category = if (selectedCategory == "All products") "" else selectedCategory,
                excludeOutOfStock = excludeOutOfStock,
                minPrice = priceRange.start.toInt(),
                maxPrice = priceRange.endInclusive.toInt()
            ) }
        ).flow.cachedIn(viewModelScope)
    }
}
