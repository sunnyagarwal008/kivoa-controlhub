package com.kivoa.controlhub.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kivoa.controlhub.api.ApiService
import com.kivoa.controlhub.data.UpdateProductRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditProductViewModel(private val apiService: ApiService) : ViewModel() {

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState = _updateState.asStateFlow()

    fun updateProduct(productId: Long, request: UpdateProductRequest) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            try {
                val response = apiService.updateProduct(productId, request)
                if (response.success) {
                    _updateState.value = UpdateState.Success
                } else {
                    _updateState.value = UpdateState.Error(response.message)
                }
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }
}

sealed class UpdateState {
    object Idle : UpdateState()
    object Loading : UpdateState()
    object Success : UpdateState()
    data class Error(val message: String) : UpdateState()
}
