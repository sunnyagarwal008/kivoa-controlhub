package com.kivoa.controlhub.ui.screens.settings

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.gson.Gson
import com.kivoa.controlhub.AppBarState
import com.kivoa.controlhub.AppBarViewModel
import com.kivoa.controlhub.Screen
import com.kivoa.controlhub.api.ApiService
import com.kivoa.controlhub.data.Prompt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CategoryPromptsViewModel(private val apiService: ApiService) : ViewModel() {

    private val _prompts = MutableStateFlow<List<Prompt>>(emptyList())
    val prompts: StateFlow<List<Prompt>> = _prompts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun getPrompts(categoryName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getPrompts(category = categoryName)
                if (response.success) {
                    _prompts.value = response.data
                    Log.d("CategoryPromptsViewModel", "Prompts fetched successfully: ${response.data}")
                } else {
                    Log.e("CategoryPromptsViewModel", "Failed to fetch prompts: ${response}")
                }
            } catch (e: Exception) {
                Log.e("CategoryPromptsViewModel", "Error fetching prompts", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class CategoryPromptsViewModelFactory(private val apiService: ApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryPromptsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryPromptsViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CategoryPromptsScreen(
    navController: NavController,
    categoryName: String,
    viewModel: CategoryPromptsViewModel,
    appBarViewModel: AppBarViewModel
) {
    val prompts by viewModel.prompts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(categoryName) {
        viewModel.getPrompts(categoryName)
        appBarViewModel.setAppBarState(
            AppBarState(
                title = { Text("Prompts for $categoryName") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(Screen.CreatePrompt.withArgs(categoryName))
            }) {
                Icon(Icons.Default.Add, contentDescription = "Create Prompt")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                if (prompts.isEmpty()) {
                    Text("No prompts found for this category.", modifier = Modifier.align(Alignment.Center))
                } else {
                    val groupedPrompts = prompts.groupBy { it.type ?: "Others" }

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        groupedPrompts.forEach { (type, prompts) ->
                            stickyHeader {
                                Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant) {
                                    Text(
                                        text = type,
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                 }
                            }
                            items(prompts) { prompt ->
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            text = prompt.text.take(100) + if (prompt.text.length > 100) "..." else "",
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    modifier = Modifier.clickable {
                                        val promptJson = Gson().toJson(prompt)
                                        navController.navigate(Screen.EditPrompt.withArgs(promptJson))
                                    },
                                    trailingContent = {
                                        Icon(
                                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = "Edit"
                                        )
                                    }
                                )
                                Divider()
                            }
                        }
                    }
                }
            }
        }
    }
}
