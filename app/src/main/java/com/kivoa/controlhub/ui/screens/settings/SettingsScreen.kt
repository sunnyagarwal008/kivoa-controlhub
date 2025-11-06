package com.kivoa.controlhub.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kivoa.controlhub.AppBarState
import com.kivoa.controlhub.AppBarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, appBarViewModel: AppBarViewModel) {
    remember { // Use LaunchedEffect or SideEffect if you need to update app bar state on recomposition
        appBarViewModel.setAppBarState(
            AppBarState(
                title = { Text("Settings") }
            )
        )
        0 // Dummy value to satisfy remember block
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ListItem(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigate("Settings/Categories") },
            headlineContent = { Text("Categories") },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Categories") }
        )
        Divider()
        // Add more settings items here
    }
}
