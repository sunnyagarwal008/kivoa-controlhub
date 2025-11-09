package com.kivoa.controlhub.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kivoa.controlhub.api.ApiService
import com.kivoa.controlhub.data.CreatePromptRequest
import com.kivoa.controlhub.data.Prompt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreatePromptViewModel(private val apiService: ApiService) : ViewModel() {
    private val _createState = MutableStateFlow<CreateState>(CreateState.Idle)
    val createState: StateFlow<CreateState> = _createState

    fun createPrompt(request: CreatePromptRequest) {
        viewModelScope.launch {
            _createState.value = CreateState.Loading
            try {
                val response = apiService.createPrompt(request)
                if (response.success) {
                    _createState.value = CreateState.Success(response.data)
                } else {
                    _createState.value = CreateState.Error(response.message)
                }
            } catch (e: Exception) {
                _createState.value = CreateState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }
}

sealed class CreateState {
    object Idle : CreateState()
    object Loading : CreateState()
    data class Success(val prompt: Prompt) : CreateState()
    data class Error(val message: String) : CreateState()
}

class CreatePromptViewModelFactory(private val apiService: ApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreatePromptViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreatePromptViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}