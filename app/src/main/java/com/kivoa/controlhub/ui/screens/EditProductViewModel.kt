package com.kivoa.controlhub.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kivoa.controlhub.api.ApiService
import com.kivoa.controlhub.data.ApiProduct
import com.kivoa.controlhub.data.UpdateProductRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UpdateState {
    object Idle : UpdateState()
    object Loading : UpdateState()
    data class Success(val product: ApiProduct) : UpdateState()
    data class Error(val message: String) : UpdateState()
}

class EditProductViewModel(private val apiService: ApiService) : ViewModel() {

    private val _product = MutableStateFlow<ApiProduct?>(null)
    val product: StateFlow<ApiProduct?> = _product.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    fun getProductById(productId: Long) {
        viewModelScope.launch {
            try {
                _product.value = apiService.getProductById(productId).data
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateProduct(productId: Long, request: UpdateProductRequest) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            try {
                val updatedProduct = apiService.updateProduct(productId, request)
                _updateState.value = UpdateState.Success(updatedProduct.data)
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error(e.message ?: "An error occurred")
            }
        }
    }
}