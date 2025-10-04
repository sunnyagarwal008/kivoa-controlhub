package com.kivoa.controlhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.SubcomposeAsyncImage
import com.google.gson.Gson
import com.kivoa.controlhub.Helper
import com.kivoa.controlhub.Screen
import com.kivoa.controlhub.ShimmerEffect
import com.kivoa.controlhub.data.Product
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun BrowseScreen(viewModel: BrowseViewModel = viewModel(), navController: NavController) {
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }

    if (selectedCategory == null) {
        CategorySelectionScreen(onCategorySelected = { selectedCategory = it })
    } else {
        var excludeOutOfStock by rememberSaveable { mutableStateOf(true) }
        ProductGridScreen(
            category = selectedCategory!!,
            viewModel = viewModel,
            navController = navController,
            excludeOutOfStock = excludeOutOfStock,
            onExcludeOutOfStockChanged = { excludeOutOfStock = it },
            onBack = { selectedCategory = null } // Go back to category selection
        )
    }
}

@Composable
fun CategorySelectionScreen(onCategorySelected: (String) -> Unit) {
    val categories = mapOf(
        "Necklace" to Color(0xFFE57373),
        "Ring" to Color(0xFF81C784),
        "Earring" to Color(0xFF64B5F6),
        "Bracelet" to Color(0xFFFFF176)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(categories.keys.toList().size) { index ->
            val category = categories.keys.toList()[index]
            val color = categories.values.toList()[index]
            CategoryCard(category = category, color = color, onClick = { onCategorySelected(category) })
        }
    }
}

@Composable
fun CategoryCard(category: String, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
        }
    }
}

@Composable
fun ProductGridScreen(
    category: String,
    viewModel: BrowseViewModel,
    navController: NavController,
    excludeOutOfStock: Boolean,
    onExcludeOutOfStockChanged: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val lazyPagingItems = viewModel.getProducts(category, excludeOutOfStock).collectAsLazyPagingItems()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(text = category)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "Exclude out of stock")
            Switch(checked = excludeOutOfStock, onCheckedChange = onExcludeOutOfStockChanged)
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (lazyPagingItems.loadState.refresh == LoadState.Loading) {
                items(10) {
                    ShimmerEffect(modifier = Modifier.aspectRatio(1f))
                }
            }

            items(lazyPagingItems.itemCount) { index ->
                lazyPagingItems[index]?.let {
                    ProductCard(product = it, onClick = {
                        val productJson = Gson().toJson(it)
                        val encodedUrl = URLEncoder.encode(productJson, StandardCharsets.UTF_8.toString())
                        navController.navigate(Screen.ProductDetail.route + "/$encodedUrl")
                    })
                }
            }

            if (lazyPagingItems.loadState.append == LoadState.Loading) {
                item {
                    ShimmerEffect(modifier = Modifier.aspectRatio(1f))
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() }
    ) {
        Column {
            SubcomposeAsyncImage(
                model = Helper.getGoogleDriveImageUrl(product.imageUrl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = product.sku, modifier = Modifier.padding(horizontal = 8.dp))
            Text(text = "₹${product.sellingPrice}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            val quantity = product.quantity.toIntOrNull() ?: 0
            val inStock = quantity > 0
            val stockText = if (inStock) "In stock" else "Out of stock"
            val stockColor = if (inStock) Color.Green else Color.Red

            Text(
                text = stockText,
                color = stockColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
