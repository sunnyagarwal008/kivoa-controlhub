package com.kivoa.controlhub.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kivoa.controlhub.api.RetrofitInstance
import com.kivoa.controlhub.data.ApiProduct
import com.kivoa.controlhub.data.UpdateProductStockRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductDetailViewModel : ViewModel() {

    private val _product = MutableStateFlow<ApiProduct?>(null)
    val product: StateFlow<ApiProduct?> = _product.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun getProductById(productId: Long) {
        viewModelScope.launch {
            _product.value = RetrofitInstance.api.getProductById(productId).data
        }
    }

    fun updateProductStock(productId: Long, inStock: Boolean) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                RetrofitInstance.api.updateProductStock(
                    productId,
                    UpdateProductStockRequest(inStock)
                )
                getProductById(productId)
            } finally {
                _isLoading.value = false
            }
        }
    }
}