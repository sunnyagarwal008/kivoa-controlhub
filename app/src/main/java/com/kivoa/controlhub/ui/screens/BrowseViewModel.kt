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
import com.kivoa.controlhub.data.ApplyDiscountRequest
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
    val discountRange: ClosedFloatingPointRange<Float> = 0f..100f,
    val excludeOutOfStock: Boolean = true,
    val sortBy: String = "created_at",
    val sortOrder: String = "desc",
    val selectedTags: Set<String> = emptySet(),
    val boxNumber: String? = null,
    val flagged: Boolean? = null
)

class BrowseViewModel : ViewModel() {

    var selectionMode by mutableStateOf(false)
    var selectedProducts by mutableStateOf(emptySet<ApiProduct>())
    var showPriceFilterSheet by mutableStateOf(false)
    var showDiscountFilterSheet by mutableStateOf(false)
    var showDiscountDialog by mutableStateOf(false)
    var showBoxNumberDialog by mutableStateOf(false)
    var applyingDiscount by mutableStateOf(false)
    val filterParams = MutableStateFlow(FilterParams())

    private val _categories = MutableStateFlow<List<ApiCategory>>(emptyList())
    val categories: StateFlow<List<ApiCategory>> = _categories.asStateFlow()

    private val _tags = MutableStateFlow<List<String>>(emptyList())
    val tags: StateFlow<List<String>> = _tags.asStateFlow()

    private val _pdfCatalogUrl = MutableStateFlow<String?>(null)
    val pdfCatalogUrl: StateFlow<String?> = _pdfCatalogUrl.asStateFlow()

    private val _discountAppliedMessage = MutableStateFlow<String?>(null)
    val discountAppliedMessage: StateFlow<String?> = _discountAppliedMessage.asStateFlow()

    private val _totalProducts = MutableStateFlow(0)
    val totalProducts: StateFlow<Int> = _totalProducts.asStateFlow()

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
                minDiscount = params.discountRange.start.toInt(),
                maxDiscount = params.discountRange.endInclusive.toInt(),
                sortBy = params.sortBy,
                sortOrder = params.sortOrder,
                tags = params.selectedTags.joinToString(","),
                boxNumber = params.boxNumber,
                flagged = params.flagged,
                onTotalCount = {
                    _totalProducts.value = it
                }
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
        filterParams.value = filterParams.value.copy(selectedCategory = category, selectedTags = emptySet())
        updateTagsForCategory(category)
    }

    private fun updateTagsForCategory(category: String) {
        if (category == "All products") {
            _tags.value = _categories.value.flatMap { it.tags?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList() }.distinct()
        } else {
            _tags.value = _categories.value.find { it.name == category }?.tags?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
        }
    }

    fun updatePriceRange(newRange: ClosedFloatingPointRange<Float>) {
        filterParams.value = filterParams.value.copy(priceRange = newRange)
    }

    fun updateDiscountRange(newRange: ClosedFloatingPointRange<Float>) {
        filterParams.value = filterParams.value.copy(discountRange = newRange)
    }

    fun updateExcludeOutOfStock(exclude: Boolean) {
        filterParams.value = filterParams.value.copy(excludeOutOfStock = exclude)
    }
    fun updateSort(sortBy: String, sortOrder: String) {
        filterParams.value = filterParams.value.copy(sortBy = sortBy, sortOrder = sortOrder)
    }

    fun updateSelectedTags(tags: Set<String>) {
        filterParams.value = filterParams.value.copy(selectedTags = tags)
    }

    fun updateBoxNumber(boxNumber: String?) {
        filterParams.value = filterParams.value.copy(boxNumber = boxNumber)
    }

    fun updateFlagged(flagged: Boolean?) {
        filterParams.value = filterParams.value.copy(flagged = flagged)
    }

    fun generatePdfCatalog(name: String, onSuccess: () -> Unit) {
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
                    name = name,
                    selectedTags = params.selectedTags.joinToString(","),
                )
                val response = RetrofitInstance.api.generatePdfCatalog(request)
                if (response.success) {
                    _pdfCatalogUrl.value = response.data.catalogUrl
                    onSuccess()
                }

            } catch (e: Exception) {
                // Handle error
            } finally {
                generatingPdf = false
            }
        }
    }

    fun applyDiscount(discount: Int) {
        viewModelScope.launch {
            applyingDiscount = true
            try {
                val params = filterParams.value
                val request = ApplyDiscountRequest(
                    discount = discount,
                    category = if (params.selectedCategory == "All products") null else params.selectedCategory,
                    excludeOutOfStock = params.excludeOutOfStock,
                    minPrice = params.priceRange.start.toInt(),
                    maxPrice = params.priceRange.endInclusive.toInt(),
                    tags = params.selectedTags.joinToString(",").ifEmpty { null },
                    boxNumber = params.boxNumber,
                    flagged = params.flagged
                )
                val response = RetrofitInstance.api.applyDiscount(request)
                if (response.success) {
                    _discountAppliedMessage.value = response.message
                }
            } catch (e: Exception) {
                // Handle error
                 _discountAppliedMessage.value = "Failed to apply discount: ${e.message}"
            } finally {
                applyingDiscount = false
            }
        }
    }

    fun onPdfShared() {
        _pdfCatalogUrl.value = null
    }

    fun onDiscountMessageShown() {
        _discountAppliedMessage.value = null
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getCategories()
                _categories.value = response.data
                updateTagsForCategory("All products")
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}