package com.kivoa.controlhub.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kivoa.controlhub.api.RetrofitInstance
import com.kivoa.controlhub.data.ApiProduct
import com.kivoa.controlhub.data.Product
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel : ViewModel() {

    private val _skuNumber = MutableStateFlow("")
    val skuNumber: StateFlow<String> = _skuNumber

    val skuPrefixes = listOf("NK", "RG", "BR", "ER")
    private val _selectedPrefix = MutableStateFlow(skuPrefixes.first())
    val selectedPrefix: StateFlow<String> = _selectedPrefix

    private val _searchResults = MutableStateFlow<List<ApiProduct>>(emptyList())
    val searchResults: StateFlow<List<ApiProduct>> = _searchResults

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    init {
        viewModelScope.launch {
            combine(selectedPrefix, skuNumber) { prefix, number ->
                if (number.length >= 3) {
                    "$prefix-$number"
                } else {
                    ""
                }
            }
                .debounce(500)
                .flatMapLatest { query ->
                    if (query.isEmpty()) {
                        flow { emit(emptyList()) }
                    } else {
                        flow {
                            _isSearching.value = true
                            try {
                                val response = RetrofitInstance.api.searchS(query)
                                emit(response.data)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                emit(emptyList<ApiProduct>())
                            }
                        }
                    }
                }
                .collect { results ->
                    _searchResults.value = results
                    _isSearching.value = false
                }
        }
    }

    fun onSkuNumberChange(number: String) {
        if (number.all { it.isDigit() }) {
            _skuNumber.value = number
        }
    }

    fun onPrefixChange(prefix: String) {
        _selectedPrefix.value = prefix
    }
}