package com.kivoa.controlhub.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kivoa.controlhub.AppBarState
import com.kivoa.controlhub.AppBarViewModel
import com.kivoa.controlhub.api.RetrofitInstance
import com.kivoa.controlhub.data.CreatePromptRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePromptScreen(
    navController: NavController,
    categoryName: String,
    appBarViewModel: AppBarViewModel
) {
    val apiService = RetrofitInstance.api
    val viewModel: CreatePromptViewModel = viewModel(factory = CreatePromptViewModelFactory(apiService))
    val createState by viewModel.createState.collectAsState()

    var text by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        appBarViewModel.setAppBarState(
            AppBarState(
                title = { Text("Create Prompt for $categoryName") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Prompt Text") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            singleLine = false
        )
        OutlinedTextField(
            value = type,
            onValueChange = { type = it },
            label = { Text("Type") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = tags,
            onValueChange = { tags = it },
            label = { Text("Tags") },
            modifier = Modifier.fillMaxWidth()
        )
        ListItem(
            headlineContent = { Text("Active") },
            trailingContent = {
                Switch(
                    checked = isActive,
                    onCheckedChange = { isActive = it }
                )
            }
        )
        Button(
            onClick = {
                viewModel.createPrompt(
                    CreatePromptRequest(
                        text = text,
                        type = type,
                        tags = tags,
                        isActive = isActive,
                        category = categoryName
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Prompt")
        }

        when (createState) {
            is CreateState.Loading -> CircularProgressIndicator()
            is CreateState.Success -> {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }

            is CreateState.Error -> {
                Text("Error: ${(createState as CreateState.Error).message}")
            }

            else -> {}
        }
    }
}
