package com.kivoa.controlhub.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.kivoa.controlhub.Helper
import com.kivoa.controlhub.data.Product
import com.kivoa.controlhub.ui.components.shimmer

@Composable
fun ProductDetailScreen(
    navController: NavController,
    product: Product
) {
    var showZoomedImage by remember { mutableStateOf(false) }

    if (showZoomedImage) {
        Dialog(onDismissRequest = { showZoomedImage = false }) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(Helper.getGoogleDriveImageUrl(product.imageUrl))
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
                    .data(Helper.getGoogleDriveImageUrl(product.imageUrl))
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
            Text(text = "Purchase Month: ${product.purchaseMonthYear}")
            Text(text = "Price Code: ${product.priceCode}")
            Text(text = "Buy Price: ₹${product.buyPrice}")
            Text(text = "GST: ₹${product.gst}")
            Text(text = "MRP: ₹${product.mrp}")
            Text(text = "Discount: ₹${product.discount}")
            Text(text = "Selling Price: ₹${product.sellingPrice}")
            val quantity = product.quantity.toIntOrNull() ?: 0
            val inStock = quantity > 0
            val stockText = if (inStock) "In stock" else "Out of stock"
            val stockColor = if (inStock) Color.Green else Color.Red

            Text(
                text = stockText,
                color = stockColor
            )
        }
    }
}
