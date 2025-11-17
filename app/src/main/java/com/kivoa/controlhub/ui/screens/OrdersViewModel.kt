package com.kivoa.controlhub.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kivoa.controlhub.api.ApiService
import com.kivoa.controlhub.data.shopify.order.Order
import com.kivoa.controlhub.data.shopify.order.OrdersPagingSource
import kotlinx.coroutines.flow.Flow

class OrdersViewModel(private val apiService: ApiService) : ViewModel() {

    val orders: Flow<PagingData<Order>> = Pager(
        config = PagingConfig(pageSize = 50),
        pagingSourceFactory = {
            OrdersPagingSource(
                apiService = apiService,
                status = "any",
                financialStatus = null,
                fulfillmentStatus = null
            )
        }
    ).flow.cachedIn(viewModelScope)
}
