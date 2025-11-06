package com.kivoa.controlhub.ui.screens.settings

import android.app.Application
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
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
import com.kivoa.controlhub.data.CategoriesApiResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.kivoa.controlhub.Screen

class CategoriesViewModel(application: Application, private val apiService: ApiService) : ViewModel() {
    private val _categories = MutableStateFlow<List<ApiCategory>>(emptyList())
    val categories: StateFlow<List<ApiCategory>> = _categories

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Removed init block call, as fetch will be triggered by LaunchedEffect

    fun fetchCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = apiService.getCategories()
                _categories.value = response.data
            } catch (e: Exception) {
                _error.value = e.message
                Log.e("CategoriesViewModel", "Error fetching categories", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class CategoriesViewModelFactory(private val application: Application, private val apiService: ApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoriesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoriesViewModel(application, apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(navController: NavController, appBarViewModel: AppBarViewModel) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val apiService = RetrofitInstance.api

    val categoriesViewModel: CategoriesViewModel = viewModel(factory = CategoriesViewModelFactory(application, apiService))
    val categories by categoriesViewModel.categories.collectAsState()
    val isLoading by categoriesViewModel.isLoading.collectAsState()
    val error by categoriesViewModel.error.collectAsState()

    LaunchedEffect(Unit) { // Use LaunchedEffect to update app bar state and fetch categories
        appBarViewModel.setAppBarState(
            AppBarState(
                title = { Text("Categories") }
            )
        )
        categoriesViewModel.fetchCategories() // Fetch categories when the screen is active
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.CreateCategory.route) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
        ) {
            if (isLoading) {
                Text("Loading categories...")
            } else if (error != null) {
                Text("Error: ${error}")
            } else {
                if (categories.isEmpty()) {
                    Text("No categories found.")
                } else {
                    LazyColumn {
                        items(categories) { category ->
                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val categoryJson = Gson().toJson(category)
                                        navController.navigate(Screen.CategoryDetail.route + "/${categoryJson}")
                                    },
                                headlineContent = { Text(category.name) },
                                supportingContent = { Text("Prefix: ${category.prefix}, SKU Seq: ${category.skuSequenceNumber}, Tags: ${category.tags}") }
                            )
                            Divider()
                        }
                    }
                }
            }
        }
    }
}
