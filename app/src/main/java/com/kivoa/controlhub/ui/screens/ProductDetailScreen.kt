package com.kivoa.controlhub.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.kivoa.controlhub.AppBarState
import com.kivoa.controlhub.AppBarViewModel
import com.kivoa.controlhub.data.Prompt
import com.kivoa.controlhub.ui.components.shimmer
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ProductDetailScreen(
    productId: Long,
    navController: NavController,
    shareViewModel: ShareViewModel,
    appBarViewModel: AppBarViewModel,
    productDetailViewModel: ProductDetailViewModel = viewModel()
) {
    var showZoomedImage by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var showGenerateImageDialog by remember { mutableStateOf(false) }
    var showDeleteImageConfirmationDialog by remember { mutableStateOf<Long?>(null) }
    val product by productDetailViewModel.product.collectAsState()
    val isLoading by productDetailViewModel.isLoading.collectAsState()
    val productNotFound by productDetailViewModel.productNotFound.collectAsState()
    var currentImageIndex by remember { mutableIntStateOf(0) }
    val error by productDetailViewModel.error.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            productDetailViewModel.clearError()
        }
    }


    val shouldRefreshState = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<Boolean>("refresh")
        ?.observeAsState()

    LaunchedEffect(shouldRefreshState?.value) {
        if (shouldRefreshState?.value == true) {
            productDetailViewModel.getProductById(productId)
            navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("refresh")
        }
    }
    LaunchedEffect(productId) {
        productDetailViewModel.getProductById(productId)
    }

    LaunchedEffect(product) {
        product?.let {
            appBarViewModel.setAppBarState(
                AppBarState(
                    title = { Text("Product Detail") },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            navController.navigate("edit_product/${it.id}")
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { shareViewModel.shareProduct(it) }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                        IconButton(onClick = { showDeleteConfirmationDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                        IconButton(onClick = { showGenerateImageDialog = true }) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = "Generate Image")
                        }
                    }
                )
            )
        }
    }
    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text("Delete Product") },
            text = { Text("Are you sure you want to delete this product?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        productDetailViewModel.deleteProduct(productId)
                        navController.navigateUp()
                        showDeleteConfirmationDialog = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showGenerateImageDialog) {
        GenerateImageDialog(
            productDetailViewModel = productDetailViewModel,
            productId = productId,
            onDismiss = { showGenerateImageDialog = false }
        )
    }

    showDeleteImageConfirmationDialog?.let { imageId ->
        AlertDialog(
            onDismissRequest = { showDeleteImageConfirmationDialog = null },
            title = { Text("Delete Image") },
            text = { Text("Are you sure you want to delete this image?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        productDetailViewModel.deleteProductImage(productId, imageId)
                        showDeleteImageConfirmationDialog = null
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteImageConfirmationDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }


    if (showZoomedImage) {
        Dialog(onDismissRequest = { showZoomedImage = false }) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(product?.images?.get(currentImageIndex)?.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Product Image",
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(modifier = Modifier.fillMaxSize().shimmer())
                }
            )
        }
    }

    if (productNotFound) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Product Not Found")
        }
    } else if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        product?.let {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (it.images.isNotEmpty()) {
                    val pagerState = rememberPagerState()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    ) {
                        HorizontalPager(
                            count = it.images.size,
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            Box(modifier = Modifier.fillMaxSize()) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(it.images[page].imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Product Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable {
                                            currentImageIndex = page
                                            showZoomedImage = true
                                        },
                                    loading = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .shimmer()
                                        )
                                    }
                                )
                                if (it.images.size > 1) {
                                    IconButton(
                                        onClick = {
                                            showDeleteImageConfirmationDialog = it.images[page].id
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete Image",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }
                        Icon(
                            imageVector = Icons.Filled.ZoomIn,
                            contentDescription = "Zoom In",
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        )
                        HorizontalPagerIndicator(
                            pagerState = pagerState,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp),
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(text = "SKU: ${it.sku}", style = MaterialTheme.typography.titleLarge)
                    Text(text = "Purchase Month: ${it.purchaseMonth}")
                    Text(text = "Product Code: ${it.priceCode}")
                    Text(text = "MRP: ₹${it.mrp}")
                    Text(text = "Discount: ${it.discount}%")
                    Text(text = "Selling Price: ₹${it.price}")
                    it.tags?.let { tags -> Text(text = "Tags: $tags") }
                    it.boxNumber?.let { boxNumber -> Text(text = "Box Number: $boxNumber") }
                    val outOfStock = !it.inStock
                    if (outOfStock) {
                        Text(
                            text = "Out of stock",
                            color = Color.Red,
                        )
                    }
                    Button(
                        onClick = {
                            productDetailViewModel.updateProductStock(
                                it.id,
                                !it.inStock
                            )
                        },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(if (it.inStock) "Mark Out of Stock" else "Mark In Stock")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GenerateImageDialog(
    productDetailViewModel: ProductDetailViewModel,
    productId: Long,
    onDismiss: () -> Unit
) {
    val prompts by productDetailViewModel.prompts.collectAsState()
    var selectedPrompt by remember { mutableStateOf<Prompt?>(null) }
    var customPrompt by remember { mutableStateOf("") }
    val product by productDetailViewModel.product.collectAsState()
    var isGenerating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(product) {
        product?.category?.let { productDetailViewModel.getPrompts(it) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generate Image") },
        text = {
            Column {
                if (isGenerating) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                Text("Select a prompt type or enter a custom prompt.")
                LazyColumn(modifier = Modifier.height(200.dp)) {
                    items(prompts) { prompt ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (selectedPrompt?.id == prompt.id),
                                    onClick = { selectedPrompt = prompt }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (selectedPrompt?.id == prompt.id),
                                onClick = { selectedPrompt = prompt }
                            )
                            Text(
                                text = prompt.type ?: "",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
                TextField(
                    value = customPrompt,
                    onValueChange = { customPrompt = it },
                    label = { Text("Custom Prompt") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    scope.launch {
                        isGenerating = true
                        val success = productDetailViewModel.generateProductImage(
                            productId,
                            selectedPrompt?.type,
                            customPrompt.ifBlank { null }
                        )
                        if (success) {
                            Toast.makeText(context, "Image generated successfully", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                        isGenerating = false
                    }
                },
                enabled = !isGenerating
            ) {
                Text("Generate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}