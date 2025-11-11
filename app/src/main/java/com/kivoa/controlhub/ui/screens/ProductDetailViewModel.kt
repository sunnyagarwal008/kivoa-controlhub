package com.kivoa.controlhub.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kivoa.controlhub.api.RetrofitInstance
import com.kivoa.controlhub.data.ApiProduct
import com.kivoa.controlhub.data.GenerateProductImageRequest
import com.kivoa.controlhub.data.ImagePriority
import com.kivoa.controlhub.data.ProductApiRepository
import com.kivoa.controlhub.data.Prompt
import com.kivoa.controlhub.data.UpdateProductStockRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductDetailViewModel : ViewModel() {

    private val productRepository = ProductApiRepository(RetrofitInstance.api)

    private val _product = MutableStateFlow<ApiProduct?>(null)
    val product: StateFlow<ApiProduct?> = _product.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _productNotFound = MutableStateFlow(false)
    val productNotFound: StateFlow<Boolean> = _productNotFound.asStateFlow()

    private val _prompts = MutableStateFlow<List<Prompt>>(emptyList())
    val prompts: StateFlow<List<Prompt>> = _prompts.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun getProductById(productId: Long) {
        viewModelScope.launch {
            try {
                _product.value = RetrofitInstance.api.getProductById(productId).data
            } catch (_: Exception) {
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

    fun updateProductImagePriorities(productId: Long, priorities: List<ImagePriority>) {
        viewModelScope.launch {
            try {
                productRepository.updateProductImagePriorities(productId, priorities)
                getProductById(productId)
            } catch (e: Exception) {
                _error.value = "Failed to update image priorities: ${e.message}"
            }
        }
    }

    fun deleteProduct(productId: Long) {
        viewModelScope.launch {
            RetrofitInstance.api.deleteProduct(productId)
        }
    }

    fun deleteProductImage(productId: Long, imageId: Long) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.rejectProductImage(productId, imageId)
                getProductById(productId)
            } catch (e: Exception) {
                _error.value = "Failed to delete image: ${e.message}"
            }
        }
    }

    fun getPrompts(category: String) {
        viewModelScope.launch {
            try {
                val allPrompts = RetrofitInstance.api.getPrompts(category = category).data
                _prompts.value = allPrompts.distinctBy { it.type }
            } catch (_: Exception) {
                // Handle error
            }
        }
    }

    suspend fun generateProductImage(productId: Long, promptType: String?, promptText: String?): Boolean {
        return try {
            val request = GenerateProductImageRequest(promptType, promptText)
            RetrofitInstance.api.generateProductImage(productId, request)
            getProductById(productId)
            true
        } catch (e: Exception) {
            _error.value = "Failed to generate image: ${e.message}"
            false
        }
    }

    fun clearError() {
        _error.value = null
    }
}