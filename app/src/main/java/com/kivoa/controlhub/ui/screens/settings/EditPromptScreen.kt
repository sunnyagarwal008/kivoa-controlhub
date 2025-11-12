package com.kivoa.controlhub.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kivoa.controlhub.AppBarState
import com.kivoa.controlhub.AppBarViewModel
import com.kivoa.controlhub.api.ApiService
import com.kivoa.controlhub.api.RetrofitInstance
import com.kivoa.controlhub.data.Prompt
import com.kivoa.controlhub.data.UpdatePromptRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EditPromptViewModel(private val apiService: ApiService) : ViewModel() {
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState

    private val _deleteState = MutableStateFlow<DeleteState>(DeleteState.Idle)
    val deleteState: StateFlow<DeleteState> = _deleteState

    fun updatePrompt(promptId: Long, request: UpdatePromptRequest) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            try {
                val response = apiService.updatePrompt(promptId, request)
                if (response.success) {
                    _updateState.value = UpdateState.Success(response.data)
                } else {
                    _updateState.value = UpdateState.Error(response.message)
                }
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun deletePrompt(promptId: Long) {
        viewModelScope.launch {
            _deleteState.value = DeleteState.Loading
            try {
                val response = apiService.deletePrompt(promptId)
                if (response.success) {
                    _deleteState.value = DeleteState.Success
                } else {
                    _deleteState.value = DeleteState.Error(response.message)
                }
            } catch (e: Exception) {
                _deleteState.value = DeleteState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }
}

sealed class UpdateState {
    object Idle : UpdateState()
    object Loading : UpdateState()
    data class Success(val prompt: Prompt) : UpdateState()
    data class Error(val message: String) : UpdateState()
}

sealed class DeleteState {
    object Idle : DeleteState()
    object Loading : DeleteState()
    object Success : DeleteState()
    data class Error(val message: String) : DeleteState()
}

class EditPromptViewModelFactory(private val apiService: ApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditPromptViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditPromptViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPromptScreen(
    navController: NavController,
    prompt: Prompt,
    appBarViewModel: AppBarViewModel
) {
    val apiService = RetrofitInstance.api
    val viewModel: EditPromptViewModel = viewModel(factory = EditPromptViewModelFactory(apiService))
    val updateState by viewModel.updateState.collectAsState()
    val deleteState by viewModel.deleteState.collectAsState()

    var text by remember { mutableStateOf(prompt.text) }
    var type by remember { mutableStateOf(prompt.type ?: "") }
    var tags by remember { mutableStateOf(prompt.tags ?: "") }
    var isActive by remember { mutableStateOf(prompt.isActive) }

    LaunchedEffect(Unit) {
        appBarViewModel.setAppBarState(
            AppBarState(
                title = { Text("Edit Prompt") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.deletePrompt(prompt.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
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
                viewModel.updatePrompt(
                    prompt.id,
                    UpdatePromptRequest(
                        text = text,
                        type = type,
                        tags = tags,
                        isActive = isActive,
                        category = null
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Update Prompt")
        }

        when (updateState) {
            is UpdateState.Loading -> CircularProgressIndicator()
            is UpdateState.Success -> {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }

            is UpdateState.Error -> {
                Text("Error: ${(updateState as UpdateState.Error).message}")
            }

            else -> {}
        }

        when (deleteState) {
            is DeleteState.Loading -> CircularProgressIndicator()
            is DeleteState.Success -> {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }

            is DeleteState.Error -> {
                Text("Error: ${(deleteState as DeleteState.Error).message}")
            }

            else -> {}
        }
    }
}