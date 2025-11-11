package com.kivoa.controlhub.ui.screens

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kivoa.controlhub.AppBarState
import com.kivoa.controlhub.AppBarViewModel
import com.kivoa.controlhub.data.ApiCatalog

@Composable
fun CatalogsScreen(
    navController: NavController,
    viewModel: CatalogsViewModel = viewModel(),
    appBarViewModel: AppBarViewModel
) {
    val context = LocalContext.current
    val inSelectionMode = viewModel.selectedCatalogs.isNotEmpty()

    BackHandler(enabled = inSelectionMode) {
        viewModel.clearSelection()
    }

    LaunchedEffect(inSelectionMode, viewModel.selectedCatalogs.size) {
        if (inSelectionMode) {
            appBarViewModel.setAppBarState(
                AppBarState(
                    title = { Text("${viewModel.selectedCatalogs.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.deleteSelectedCatalogs() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                )
            )
        } else {
            appBarViewModel.setAppBarState(
                AppBarState(
                    title = { Text("All Catalogs") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (viewModel.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (viewModel.errorMessage != null) {
            Text(
                text = viewModel.errorMessage!!,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(viewModel.catalogs, key = { it.id }) { catalog ->
                    val isSelected = viewModel.selectedCatalogs.contains(catalog.id)
                    CatalogItem(
                        catalog = catalog,
                        isSelected = isSelected,
                        onDownload = { downloadCatalog(context, it) },
                        onRefresh = { viewModel.refreshCatalog(it) },
                        onClick = {
                            if (inSelectionMode) {
                                viewModel.onCatalogSelected(catalog.id)
                            }
                        },
                        onLongClick = { viewModel.onCatalogSelected(catalog.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CatalogItem(
    catalog: ApiCatalog,
    isSelected: Boolean,
    onDownload: (ApiCatalog) -> Unit,
    onRefresh: (Long) -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color.LightGray else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = catalog.name)
            }
            Row {
                IconButton(onClick = { onDownload(catalog) }) {
                    Icon(Icons.Default.Download, contentDescription = "Download")
                }
                IconButton(onClick = { onRefresh(catalog.id) }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        }
    }
}

private fun downloadCatalog(context: Context, catalog: ApiCatalog) {
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val request = DownloadManager.Request(catalog.s3Url.toUri())
        .setTitle(catalog.name)
        .setDescription("Downloading")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${catalog.name}.pdf")
    downloadManager.enqueue(request)
}