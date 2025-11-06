package com.kivoa.controlhub.ui.screens.settings

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.gson.Gson
import com.kivoa.controlhub.AppBarState
import com.kivoa.controlhub.AppBarViewModel
import com.kivoa.controlhub.api.ApiService
import com.kivoa.controlhub.api.RetrofitInstance
import com.kivoa.controlhub.data.ApiCategory
import com.kivoa.controlhub.data.UpdateCategoryRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditCategoryViewModel(application: Application, private val apiService: ApiService, private val initialCategory: ApiCategory) : ViewModel() {

    private val _name = MutableStateFlow(initialCategory.name)
    val name: StateFlow<String> = _name.asStateFlow()

    private val _prefix = MutableStateFlow(initialCategory.prefix)
    val prefix: StateFlow<String> = _prefix.asStateFlow()

    private val _skuSequenceNumber = MutableStateFlow(initialCategory.skuSequenceNumber.toString())
    val skuSequenceNumber: StateFlow<String> = _skuSequenceNumber.asStateFlow()

    private val _tags = MutableStateFlow(initialCategory.tags)
    val tags: StateFlow<String?> = _tags.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    fun onNameChange(newName: String) {
        _name.value = newName
        _error.value = null
    }

    fun onPrefixChange(newPrefix: String) {
        _prefix.value = newPrefix
        _error.value = null
    }

    fun onSkuSequenceNumberChange(newSkuSequenceNumber: String) {
        _skuSequenceNumber.value = newSkuSequenceNumber
        _error.value = null
    }

    fun onTagsChange(newTags: String) {
        _tags.value = newTags
        _error.value = null
    }

    fun updateCategory(onSuccess: () -> Unit) {
        _error.value = null
        _isSuccess.value = false

        if (_name.value.isBlank() || _prefix.value.isBlank() || _skuSequenceNumber.value.isBlank() || _tags.value?.isBlank() == true) {
            _error.value = "All fields are required."
            return
        }

        val skuNumber = _skuSequenceNumber.value.toIntOrNull()
        if (skuNumber == null) {
            _error.value = "SKU Sequence Number must be a valid number."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = UpdateCategoryRequest(
                    name = _name.value.trim(),
                    prefix = _prefix.value,
                    skuSequenceNumber = skuNumber,
                    tags = _tags.value
                )
                apiService.updateCategory(initialCategory.id, request)
                _isSuccess.value = true
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "An unknown error occurred."
                Log.e("EditCategoryViewModel", "Error updating category", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class EditCategoryViewModelFactory(private val application: Application, private val apiService: ApiService, private val initialCategory: ApiCategory) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditCategoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditCategoryViewModel(application, apiService, initialCategory) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategoryScreen(navController: NavController, appBarViewModel: AppBarViewModel, category: ApiCategory, onCategoryUpdated: () -> Unit) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val apiService = RetrofitInstance.api

    val viewModel: EditCategoryViewModel = viewModel(factory = EditCategoryViewModelFactory(application, apiService, category))

    val name by viewModel.name.collectAsState()
    val prefix by viewModel.prefix.collectAsState()
    val skuSequenceNumber by viewModel.skuSequenceNumber.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    LaunchedEffect(Unit) {
        appBarViewModel.setAppBarState(
            AppBarState(
                title = { Text("Edit Category") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        )
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            onCategoryUpdated()
            navController.popBackStack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = viewModel::onNameChange,
            label = { Text("Category Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = prefix,
            onValueChange = viewModel::onPrefixChange,
            label = { Text("Prefix") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = skuSequenceNumber,
            onValueChange = viewModel::onSkuSequenceNumberChange,
            label = { Text("SKU Sequence Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = tags ?: "",
            onValueChange = viewModel::onTagsChange,
            label = { Text("Tags (comma-separated)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { viewModel.updateCategory(onCategoryUpdated) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "Updating..." else "Update Category")
        }
        error?.let { errorMessage ->
            Text(errorMessage, color = androidx.compose.ui.graphics.Color.Red, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
