package com.kivoa.controlhub.ui.screens

import android.util.Log
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
import com.kivoa.controlhub.data.ApiProduct
import com.kivoa.controlhub.data.ProductPagingSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

class BrowseViewModel : ViewModel() {

    data class FilterParams(
        val selectedCategory: String,
        val excludeOutOfStock: Boolean,
        val priceRange: ClosedFloatingPointRange<Float>
    )

    private val _filterParams = MutableStateFlow(
        FilterParams(
            selectedCategory = "All products",
            excludeOutOfStock = true,
            priceRange = 0f..5000f
        )
    )
    val filterParams: StateFlow<FilterParams> = _filterParams.asStateFlow()

    var selectionMode by mutableStateOf(false)
    var selectedProducts by mutableStateOf<Set<ApiProduct>>(emptySet())
    var showPriceFilterDialog by mutableStateOf(false)


    fun onProductClicked(product: ApiProduct) {
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

    fun onProductLongClicked(product: ApiProduct) {
        if (!selectionMode) {
            selectionMode = true
            selectedProducts = setOf(product)
        }
    }

    fun updateSelectedCategory(category: String) {
        _filterParams.update { it.copy(selectedCategory = category) }
    }

    fun updateExcludeOutOfStock(exclude: Boolean) {
        _filterParams.update { it.copy(excludeOutOfStock = exclude) }
    }

    fun updatePriceRange(price: ClosedFloatingPointRange<Float>) {
        _filterParams.update { it.copy(priceRange = price) }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    val products: Flow<PagingData<ApiProduct>> =
        _filterParams
            .flatMapLatest { (category, exclude, price) ->
                Pager(
                    config = PagingConfig(pageSize = 10, enablePlaceholders = true),
                    pagingSourceFactory = {
                        ProductPagingSource(
                            apiService = RetrofitInstance.api,
                            category = if (category == "All products") null else category,
                            excludeOutOfStock = exclude,
                            minPrice = price.start.toInt(),
                            maxPrice = price.endInclusive.toInt()
                        )
                    }
                ).flow
            }.cachedIn(viewModelScope)
}