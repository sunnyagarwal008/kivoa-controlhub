package com.kivoa.controlhub.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import kotlinx.coroutines.flow.flatMapLatest

class BrowseViewModel : ViewModel() {

    var selectedCategory by mutableStateOf("All products")
    var excludeOutOfStock by mutableStateOf(true)
    var priceRange by mutableStateOf(0f..5000f)
    var showPriceFilterDialog by mutableStateOf(false)

    var selectionMode by mutableStateOf(false)
    var selectedProducts by mutableStateOf<Set<Product>>(emptySet())


    fun onProductClicked(product: Product) {
        if (selectionMode) {
            val currentSelection = selectedProducts.toMutableSet()
            if (product in currentSelection) {
                currentSelection.remove(product)
            } else if (currentSelection.size < 10) {
                currentSelection.add(product)
            }
            selectedProducts = currentSelection
            if (currentSelection.isEmpty()) {
                selectionMode = false
            }
        }
    }

    fun onProductLongClicked(product: Product) {
        if (!selectionMode) {
            selectionMode = true
            selectedProducts = setOf(product)
        }
    }


    val products: Flow<PagingData<Product>> = snapshotFlow {
        Triple(selectedCategory, excludeOutOfStock, priceRange)
    }.flatMapLatest { (category, exclude, price) ->
        Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                ProductPagingSource(
                    apiService = RetrofitInstance.api,
                    category = if (category == "All products") "" else category,
                    excludeOutOfStock = exclude,
                    minPrice = price.start.toInt(),
                    maxPrice = price.endInclusive.toInt()
                )
            }
        ).flow
    }.cachedIn(viewModelScope)
}