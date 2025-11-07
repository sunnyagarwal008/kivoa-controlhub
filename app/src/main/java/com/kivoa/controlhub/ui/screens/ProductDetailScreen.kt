package com.kivoa.controlhub.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.kivoa.controlhub.AppBarState
import com.kivoa.controlhub.AppBarViewModel
import com.kivoa.controlhub.ui.components.shimmer

@Composable
fun ProductDetailScreen(
    productId: Long,
    navController: NavController,
    shareViewModel: ShareViewModel,
    appBarViewModel: AppBarViewModel,
    productDetailViewModel: ProductDetailViewModel = viewModel()
) {
    var showZoomedImage by remember { mutableStateOf(false) }
    val product by productDetailViewModel.product.collectAsState()
    val isLoading by productDetailViewModel.isLoading.collectAsState()
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
                    }
                )
            )
        }
    }

    if (showZoomedImage) {
        Dialog(onDismissRequest = { showZoomedImage = false }) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(product?.images?.first()?.imageUrl)
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

    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        product?.let {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(it.images.first().imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Product Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { showZoomedImage = true },
                        loading = {
                            Box(modifier = Modifier.fillMaxSize().shimmer())
                        }
                    )
                    Icon(
                        imageVector = Icons.Filled.ZoomIn,
                        contentDescription = "Zoom In",
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    )
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