package com.kivoa.controlhub.ui.screens.settings

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.gson.Gson
import com.kivoa.controlhub.AppBarState
import com.kivoa.controlhub.AppBarViewModel
import com.kivoa.controlhub.Screen
import com.kivoa.controlhub.api.RetrofitInstance
import com.kivoa.controlhub.data.ApiCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(navController: NavController, appBarViewModel: AppBarViewModel, categoryId: String) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val apiService = RetrofitInstance.api

    val viewModel: CategoryDetailViewModel = viewModel(factory = CategoryDetailViewModelFactory(application, apiService, categoryId))
    val category by viewModel.category.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(categoryId, category) {
        appBarViewModel.setAppBarState(
            AppBarState(
                title = { Text(category?.name ?: "Category Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (error != null) {
                Text("Error: ${error}", color = MaterialTheme.colorScheme.error)
            } else if (category != null) {
                val currentCategory = category as ApiCategory
                Text("ID: ${currentCategory.id}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Name: ${currentCategory.name}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Prefix: ${currentCategory.prefix}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("SKU Sequence Number: ${currentCategory.skuSequenceNumber}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tags: ${currentCategory.tags}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Created At: ${currentCategory.createdAt}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Updated At: ${currentCategory.updatedAt}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val categoryJson = Gson().toJson(currentCategory)
                        navController.navigate(Screen.EditCategory.route + "${categoryJson}")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Edit Category")
                }
            } else {
                Text("Category not found.")
            }
        }
    }
}
