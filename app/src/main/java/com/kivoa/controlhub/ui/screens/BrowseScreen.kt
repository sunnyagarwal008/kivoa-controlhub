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
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(viewModel: BrowseViewModel = viewModel(), navController: NavController) {
    val categories = listOf("All products", "Necklace", "Ring", "Earring", "Bracelet")

    val lazyPagingItems = viewModel.getProducts().collectAsLazyPagingItems()

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Category Dropdown
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    readOnly = true,
                    value = viewModel.selectedCategory,
                    onValueChange = {},
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    categories.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                viewModel.selectedCategory = selectionOption
                                expanded = false
                            },
                        )
                    }
                }
            }

            // Price Range Slider
            Text(text = "Price Range: ₹${viewModel.priceRange.start.toInt()} - ₹${viewModel.priceRange.endInclusive.toInt()}")
            RangeSlider(
                value = viewModel.priceRange,
                onValueChange = { viewModel.priceRange = it },
                valueRange = 100f..4000f,
                steps = 38
            )

            // Exclude out of stock switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Exclude out of stock")
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = viewModel.excludeOutOfStock,
                    onCheckedChange = { viewModel.excludeOutOfStock = it }
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f) // Use weight to take remaining space
        ) {
            if (lazyPagingItems.loadState.refresh == LoadState.Loading) {
                items(10) {
                    ShimmerEffect(modifier = Modifier.aspectRatio(0.7f))
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
                    ShimmerEffect(modifier = Modifier.aspectRatio(0.7f))
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
            .aspectRatio(0.7f)
            .clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            SubcomposeAsyncImage(
                model = Helper.getGoogleDriveImageUrl(product.imageUrl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                            startY = 0f,
                            endY = 400f,
                        )
                    )
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.sku,
                    color = Color.White
                )
                Text(
                    text = "₹${product.sellingPrice}",
                    color = Color.White
                )
                val quantity = product.quantity.toIntOrNull() ?: 0
                val outOfStock = quantity == 0
                if (outOfStock) {
                    Text(
                        text = "Out of stock",
                        color = Color.Red
                    )
                }
            }
        }
    }
}
