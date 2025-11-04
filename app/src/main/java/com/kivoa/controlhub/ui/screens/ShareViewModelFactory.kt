package com.kivoa.controlhub.ui.screens

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kivoa.controlhub.api.ApiService

class ShareViewModelFactory(private val application: Application, private val apiService: ApiService, private val onRefreshProducts: (() -> Unit)? = null) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShareViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShareViewModel(application, apiService, onRefreshProducts) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
