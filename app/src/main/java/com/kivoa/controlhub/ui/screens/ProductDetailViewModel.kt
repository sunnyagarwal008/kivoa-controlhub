package com.kivoa.controlhub.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kivoa.controlhub.api.RetrofitInstance
import com.kivoa.controlhub.data.ApiProduct
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductDetailViewModel : ViewModel() {

    private val _product = MutableStateFlow<ApiProduct?>(null)
    val product: StateFlow<ApiProduct?> = _product.asStateFlow()

    fun getProductById(productId: Long) {
        viewModelScope.launch {
            try {
                _product.value = RetrofitInstance.api.getProductById(productId).data
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}