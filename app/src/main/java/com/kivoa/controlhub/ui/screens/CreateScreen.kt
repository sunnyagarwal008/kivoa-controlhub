package com.kivoa.controlhub.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.kivoa.controlhub.AppBarState
import com.kivoa.controlhub.AppBarViewModel
import com.kivoa.controlhub.Screen
import com.kivoa.controlhub.data.ApiRawImage
import com.kivoa.controlhub.data.RawProduct
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CreateScreen(
    createViewModel: CreateViewModel = viewModel(),
    appBarViewModel: AppBarViewModel,
    navController: NavController
) {
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Pending", "In Progress", "In Review")

    val rawProducts = createViewModel.rawProducts.collectAsLazyPagingItems()
    val isLoading by createViewModel.isLoading.collectAsState()
    val bulkProductCreationSuccess by createViewModel.bulkProductCreationSuccess.collectAsState()
    val inReviewProducts by createViewModel.inReviewProducts.collectAsState()
    val inReviewProductsLoading by createViewModel.inReviewProductsLoading.collectAsState()
    val inProgressProducts by createViewModel.inProgressProducts.collectAsState()
    val inProgressProductsLoading by createViewModel.inProgressProductsLoading.collectAsState()
    val selectedInReviewProductIds by createViewModel.selectedInReviewProductIds.collectAsState()
    val selectedRawProductIds by createViewModel.selectedRawProductIds.collectAsState()

    val context = LocalContext.current

    var showFullScreenImageDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedProductUris by remember { mutableStateOf<PersistentList<Uri>>(persistentListOf()) }
    var showCreateProductFormsDialog by remember { mutableStateOf(false) }

    if (bulkProductCreationSuccess && showCreateProductFormsDialog) {
        showCreateProductFormsDialog = false
        selectedProductUris = persistentListOf()
        createViewModel.resetBulkProductCreationSuccess()
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        createViewModel.onImagesSelected(uris, context)
    }

    LaunchedEffect(tabIndex) {
        when (tabIndex) {
            1 -> createViewModel.fetchInProgressProducts()
            2 -> createViewModel.fetchInReviewProducts()
        }
    }

    LaunchedEffect(selectedRawProductIds.isNotEmpty(), selectedInReviewProductIds.isNotEmpty(), tabIndex) {
        val currentAppBarState = when (tabIndex) {
            0 -> {
                if (selectedRawProductIds.isNotEmpty()) {
                    AppBarState(
                        title = { Text("Selected ${selectedRawProductIds.size} images") },
                        navigationIcon = {
                            IconButton(onClick = { createViewModel.clearSelectedRawProductIds() }) {
                                Icon(Icons.Default.Close, "Clear selection")
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                createViewModel.deleteRawProducts()
                            }) {
                                Icon(Icons.Default.Delete, "Delete Products")
                            }
                            IconButton(onClick = { showCreateProductFormsDialog = true }) {
                                Icon(Icons.Default.Done, "Create Products")
                            }
                        }
                    )
                } else {
                    AppBarState(title = { Text(tabs[tabIndex]) })
                }
            }
            2 -> {
                if (selectedInReviewProductIds.isNotEmpty()) {
                    AppBarState(
                        title = { Text("Selected ${selectedInReviewProductIds.size} products") },
                        navigationIcon = {
                            IconButton(onClick = { createViewModel.clearSelectedInReviewProductIds() }) {
                                Icon(Icons.Default.Close, "Clear selection")
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                createViewModel.updateProductsStatus(selectedInReviewProductIds.toList(), "live")
                            }) {
                                Icon(Icons.Default.Check, "Mark as Live")
                            }
                            IconButton(onClick = {
                                createViewModel.deleteProducts(selectedInReviewProductIds.toList())
                            }) {
                                Icon(Icons.Default.Delete, "Delete Products")
                            }
                        }
                    )
                } else {
                    AppBarState(title = { Text(tabs[tabIndex]) })
                }
            }
            else -> AppBarState(title = { Text(tabs[tabIndex]) })
        }
        appBarViewModel.setAppBarState(currentAppBarState)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = tabIndex == index,
                    onClick = { tabIndex = index }
                )
            }
        }

        when (tabIndex) {
            0 -> {
                PendingProductsTab(
                    rawProducts = rawProducts,
                    isLoading = isLoading,
                    onImageClick = { uri ->
                        selectedImageUri = uri
                        showFullScreenImageDialog = true
                    },
                    selectedProductIds = selectedRawProductIds,
                    onProductLongPress = { id, isSelected ->
                        createViewModel.updateSelectedRawProductIds(id, isSelected)
                    },
                    imagePickerLauncher = imagePickerLauncher
                )
            }

            1 -> {
                InProgressProductsTab(
                    inProgressProducts = inProgressProducts,
                    isLoading = inProgressProductsLoading,
                    onProductClick = { product ->
                        selectedImageUri = (product.images.firstOrNull()?.imageUrl ?: product.rawImage).toUri()
                        showFullScreenImageDialog = true
                    }
                )
            }

            2 -> {
                InReviewProductsTab(
                    inReviewProducts = inReviewProducts,
                    isLoading = inReviewProductsLoading,
                    onProductClick = { product ->
                        if (selectedInReviewProductIds.isNotEmpty()) {
                            createViewModel.updateSelectedInReviewProductIds(
                                product.id,
                                !selectedInReviewProductIds.contains(product.id)
                            )
                        } else {
                            navController.navigate(Screen.EditProduct.route + "/${product.id}")
                        }
                    },
                    selectedProductIds = selectedInReviewProductIds,
                    onProductLongPress = { productId, isSelected ->
                        createViewModel.updateSelectedInReviewProductIds(productId, isSelected)
                    }
                )
            }
        }
    }

    if (showFullScreenImageDialog && selectedImageUri != null) {
        FullScreenImageDialog(
            imageUri = selectedImageUri!!,
            onDismiss = { showFullScreenImageDialog = false }
        )
    }

    if (showCreateProductFormsDialog) {
        val selectedRawProducts = rawProducts.itemSnapshotList.items
            .filter { product: ApiRawImage ->
                selectedRawProductIds.contains(product.id)
            }.map { RawProduct(imageUri = it.imageUrl) }
        CreateProductFormsDialog(
            selectedRawProducts = selectedRawProducts,
            onDismiss = { showCreateProductFormsDialog = false },
            createViewModel = createViewModel,
            onProductCreationSuccess = {
                showCreateProductFormsDialog = false
                selectedProductUris = persistentListOf()
            }
        )
    }
}