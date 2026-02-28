package com.kivoa.controlhub.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kivoa.controlhub.api.RetrofitInstance
import com.kivoa.controlhub.data.AmazonProduct
import com.kivoa.controlhub.data.AmazonProductRepository
import kotlinx.coroutines.flow.Flow

class AmazonProductViewModel : ViewModel() {

    private val repository = AmazonProductRepository(RetrofitInstance.api)

    fun getAmazonProducts(): Flow<PagingData<AmazonProduct>> {
        return repository.getAmazonProducts().cachedIn(viewModelScope)
    }
}