package com.kivoa.controlhub.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kivoa.controlhub.AppBarState
import com.kivoa.controlhub.AppBarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    appBarViewModel: AppBarViewModel,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    remember {
        appBarViewModel.setAppBarState(
            AppBarState(
                title = { Text("Settings") }
            )
        )
        0
    }

    val theme by settingsViewModel.theme.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = theme,
            onThemeSelected = { newTheme ->
                settingsViewModel.setTheme(newTheme)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ListItem(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigate("Settings/Categories") },
            headlineContent = { Text("Categories") },
            trailingContent = {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Categories"
                )
            }
        )
        Divider()
        ListItem(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showThemeDialog = true },
            headlineContent = { Text("Theme") },
            supportingContent = { Text(theme) }
        )
        Divider()
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: String,
    onThemeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val themes = listOf("Light", "Dark", "System")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose a theme") },
        text = {
            Column {
                themes.forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(theme) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (theme == currentTheme),
                            onClick = { onThemeSelected(theme) }
                        )
                        Text(
                            text = theme,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}