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
import com.kivoa.controlhub.data.ApiCategory
import com.kivoa.controlhub.data.ApiProduct
import com.kivoa.controlhub.data.GeneratePdfCatalogRequest
import com.kivoa.controlhub.data.ProductPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

data class FilterParams(
    val selectedCategory: String = "All products",
    val priceRange: ClosedFloatingPointRange<Float> = 0f..5000f,
    val excludeOutOfStock: Boolean = true,
    val sortBy: String = "created_at",
    val sortOrder: String = "desc"
)

class BrowseViewModel : ViewModel() {

    var selectionMode by mutableStateOf(false)
    var selectedProducts by mutableStateOf(emptySet<ApiProduct>())
    var showPriceFilterDialog by mutableStateOf(false)
    val filterParams = MutableStateFlow(FilterParams())

    private val _categories = MutableStateFlow<List<ApiCategory>>(emptyList())
    val categories: StateFlow<List<ApiCategory>> = _categories.asStateFlow()

    private val _pdfCatalogUrl = MutableStateFlow<String?>(null)
    val pdfCatalogUrl: StateFlow<String?> = _pdfCatalogUrl.asStateFlow()

    var generatingPdf by mutableStateOf(false)

    init {
        fetchCategories()
    }

    val products: Flow<PagingData<ApiProduct>> = filterParams.flatMapLatest { params ->
        Pager(PagingConfig(pageSize = 10)) {
            ProductPagingSource(
                apiService = RetrofitInstance.api,
                category = if (params.selectedCategory == "All products") null else params.selectedCategory,
                excludeOutOfStock = params.excludeOutOfStock,
                minPrice = params.priceRange.start.toInt(),
                maxPrice = params.priceRange.endInclusive.toInt(),
                sortBy = params.sortBy,
                sortOrder = params.sortOrder
            )
        }.flow
    }.cachedIn(viewModelScope)

    fun onProductClicked(product: ApiProduct) {
        if (selectionMode) {
            selectedProducts = if (selectedProducts.contains(product)) {
                selectedProducts - product
            } else {
                selectedProducts + product
            }
        }
    }

    fun onProductLongClicked(product: ApiProduct) {
        selectionMode = true
        selectedProducts = selectedProducts + product
    }

    fun updateSelectedCategory(category: String) {
        filterParams.value = filterParams.value.copy(selectedCategory = category)
    }

    fun updatePriceRange(newRange: ClosedFloatingPointRange<Float>) {
        filterParams.value = filterParams.value.copy(priceRange = newRange)
    }

    fun updateExcludeOutOfStock(exclude: Boolean) {
        filterParams.value = filterParams.value.copy(excludeOutOfStock = exclude)
    }
    fun updateSort(sortBy: String, sortOrder: String) {
        filterParams.value = filterParams.value.copy(sortBy = sortBy, sortOrder = sortOrder)
    }

    fun generatePdfCatalog(name: String) {
        viewModelScope.launch {
            generatingPdf = true
            try {
                val params = filterParams.value
                val request = GeneratePdfCatalogRequest(
                    category = if (params.selectedCategory == "All products") null else params.selectedCategory,
                    excludeOutOfStock = params.excludeOutOfStock,
                    minPrice = params.priceRange.start.toInt(),
                    maxPrice = params.priceRange.endInclusive.toInt(),
                    sortBy = params.sortBy,
                    sortOrder = params.sortOrder,
                    name = name
                )
                val response = RetrofitInstance.api.generatePdfCatalog(request)
                if (response.success) {
                    _pdfCatalogUrl.value = response.data.catalogUrl
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                generatingPdf = false
            }
        }
    }

    fun onPdfShared() {
        _pdfCatalogUrl.value = null
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getCategories()
                _categories.value = response.data
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
