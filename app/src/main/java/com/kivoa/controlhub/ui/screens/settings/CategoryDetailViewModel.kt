package com.kivoa.controlhub.ui.screens.settings

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kivoa.controlhub.api.ApiService
import com.kivoa.controlhub.data.ApiCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoryDetailViewModel(application: Application, private val apiService: ApiService, private val categoryId: String) : ViewModel() {

    private val _category = MutableStateFlow<ApiCategory?>(null)
    val category: StateFlow<ApiCategory?> = _category.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        fetchCategoryDetail()
    }

    fun fetchCategoryDetail() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val fetchedCategory = apiService.getCategoryById(categoryId)
                _category.value = fetchedCategory
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load category details."
                Log.e("CategoryDetailViewModel", "Error fetching category detail for ID: $categoryId", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class CategoryDetailViewModelFactory(private val application: Application, private val apiService: ApiService, private val categoryId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryDetailViewModel(application, apiService, categoryId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
