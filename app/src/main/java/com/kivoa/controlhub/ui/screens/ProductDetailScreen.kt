package com.kivoa.controlhub.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.google.gson.Gson
import com.kivoa.controlhub.AppBarState
import com.kivoa.controlhub.AppBarViewModel
import com.kivoa.controlhub.data.ApiProduct
import com.kivoa.controlhub.ui.components.shimmer
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun ProductDetailScreen(
    product: ApiProduct,
    navController: NavController,
    shareViewModel: ShareViewModel,
    appBarViewModel: AppBarViewModel
) {
    var showZoomedImage by remember { mutableStateOf(false) }

    LaunchedEffect(product) {
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
                        val productJson = Gson().toJson(product)
                        val encodedProductJson = URLEncoder.encode(productJson, StandardCharsets.UTF_8.toString())
                        navController.navigate("edit_product/$encodedProductJson")
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { shareViewModel.shareProduct(product) }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        )
    }

    if (showZoomedImage) {
        Dialog(onDismissRequest = { showZoomedImage = false }) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(product.images.first().imageUrl)
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

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(3f)
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(product.images.first().imageUrl)
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
                .weight(1f)
                .padding(16.dp)
        ) {
            Text(text = "SKU: ${product.sku}", style = MaterialTheme.typography.titleLarge)
            Text(text = "Purchase Month: ${product.purchaseMonth}")
            Text(text = "Product Code: ${product.priceCode}")
            Text(text = "MRP: ₹${product.mrp}")
            Text(text = "Discount: ${product.discount}%")
            Text(text = "Selling Price: ₹${product.price}")
            product.tags?.let { Text(text = "Tags: $it") }
            product.boxNumber?.let { Text(text = "Box Number: $it") }
            val outOfStock = !product.inStock
            if (outOfStock) {
                Text(
                    text = "Out of stock",
                    color = Color.Red,
                )
            }
        }
    }
}
