package com.kivoa.controlhub.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kivoa.controlhub.api.RetrofitInstance
import com.kivoa.controlhub.data.ApiCatalog
import kotlinx.coroutines.launch

class CatalogsViewModel : ViewModel() {
    private val apiService = RetrofitInstance.api

    var catalogs by mutableStateOf<List<ApiCatalog>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var selectedCatalogs by mutableStateOf(emptySet<Long>())
        private set

    init {
        fetchAllCatalogs()
    }

    fun onCatalogSelected(catalogId: Long) {
        selectedCatalogs = if (selectedCatalogs.contains(catalogId)) {
            selectedCatalogs - catalogId
        } else {
            selectedCatalogs + catalogId
        }
    }

    fun clearSelection() {
        selectedCatalogs = emptySet()
    }


    fun fetchAllCatalogs() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = apiService.getAllCatalogs()
                if (response.success) {
                    catalogs = response.data
                } else {
                    errorMessage = "Failed to fetch catalogs"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteSelectedCatalogs() {
        viewModelScope.launch {
            try {
                selectedCatalogs.forEach { apiService.deleteCatalog(it) }
                fetchAllCatalogs()
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                clearSelection()
            }
        }
    }


    fun refreshCatalog(catalogId: Long) {
        viewModelScope.launch {
            try {
                val response = apiService.refreshCatalog(catalogId)
                if (response.success) {
                    // Refresh the list to show the updated catalog
                    fetchAllCatalogs()
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            }
        }
    }
}
