package com.kivoa.controlhub.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kivoa.controlhub.api.RetrofitInstance
import com.kivoa.controlhub.data.ApiProduct
import com.kivoa.controlhub.data.GenerateProductImageRequest
import com.kivoa.controlhub.data.Prompt
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

    private val _productNotFound = MutableStateFlow(false)
    val productNotFound: StateFlow<Boolean> = _productNotFound.asStateFlow()

    private val _prompts = MutableStateFlow<List<Prompt>>(emptyList())
    val prompts: StateFlow<List<Prompt>> = _prompts.asStateFlow()

    fun getProductById(productId: Long) {
        viewModelScope.launch {
            try {
                _product.value = RetrofitInstance.api.getProductById(productId).data
            } catch (e: Exception) {
                _productNotFound.value = true
            }
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

    fun deleteProduct(productId: Long) {
        viewModelScope.launch {
            RetrofitInstance.api.deleteProduct(productId)
        }
    }

    fun getPrompts(category: String) {
        viewModelScope.launch {
            try {
                _prompts.value = RetrofitInstance.api.getPrompts(category = category).data
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun generateProductImage(productId: Long, promptType: String?, promptText: String?) {
        viewModelScope.launch {
            try {
                val request = GenerateProductImageRequest(promptType, promptText)
                RetrofitInstance.api.generateProductImage(productId, request)
                // Refresh product data
                getProductById(productId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}