package com.kivoa.controlhub.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.gson.Gson
import com.kivoa.controlhub.AppBarState
import com.kivoa.controlhub.AppBarViewModel
import com.kivoa.controlhub.Screen
import com.kivoa.controlhub.data.ApiCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(navController: NavController, appBarViewModel: AppBarViewModel, categoryJson: String?) {
    val category = categoryJson?.let { Gson().fromJson(it, ApiCategory::class.java) }

    LaunchedEffect(category) {
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
            if (category != null) {
                Text("ID: ${category.id}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Name: ${category.name}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Prefix: ${category.prefix}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("SKU Sequence Number: ${category.skuSequenceNumber}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tags: ${category.tags}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Created At: ${category.createdAt}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Updated At: ${category.updatedAt}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val categoryJsonForEdit = Gson().toJson(category)
                        navController.navigate(Screen.EditCategory.route + "/${categoryJsonForEdit}")
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
