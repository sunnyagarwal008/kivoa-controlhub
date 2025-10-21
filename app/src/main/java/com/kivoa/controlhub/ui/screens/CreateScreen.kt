package com.kivoa.controlhub.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun CreateScreen() {
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("In Review", "Pending")

    Column {
        TabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(text = { Text(title) },
                    selected = tabIndex == index,
                    onClick = { tabIndex = index }
                )
            }
        }
        when (tabIndex) {
            0 -> {
                // Content for "In Review" tab
            }
            1 -> {
                // Content for "Pending" tab
            }
        }
    }
}
