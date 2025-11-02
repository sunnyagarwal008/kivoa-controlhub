package com.kivoa.controlhub.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.SubcomposeAsyncImage
import com.google.gson.Gson
import com.kivoa.controlhub.AppBarState
import com.kivoa.controlhub.AppBarViewModel
import com.kivoa.controlhub.Screen
import com.kivoa.controlhub.ShimmerEffect
import com.kivoa.controlhub.data.ApiProduct
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BrowseScreen(
    browseViewModel: BrowseViewModel = viewModel(),
    shareViewModel: ShareViewModel = viewModel(),
    navController: NavController,
    appBarViewModel: AppBarViewModel
) {
    val categories = listOf("All products", "Necklace", "Ring", "Earring", "Bracelet")
    val lazyPagingItems = browseViewModel.products.collectAsLazyPagingItems()

    LaunchedEffect(browseViewModel.selectionMode, browseViewModel.selectedProducts.size) {
        appBarViewModel.setAppBarState(
            AppBarState(
                title = {
                    if (browseViewModel.selectionMode) {
                        Text("${browseViewModel.selectedProducts.size} selected")
                    } else {
                        Text(Screen.Browse.route)
                    }
                },
                navigationIcon = {
                    if (browseViewModel.selectionMode) {
                        IconButton(onClick = {
                            browseViewModel.selectionMode = false
                            browseViewModel.selectedProducts = emptySet()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    if (browseViewModel.selectionMode) {
                        IconButton(onClick = { shareViewModel.shareProducts(browseViewModel.selectedProducts) }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                    }
                }
            )
        )
    }

    if (browseViewModel.showPriceFilterDialog) {
        PriceFilterDialog(viewModel = browseViewModel)
    }

    if (shareViewModel.shareState is ShareViewModel.ShareState.Processing) {
        Dialog(onDismissRequest = {}) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Preparing images...")
                    CircularProgressIndicator()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            var categoryExpanded by remember { mutableStateOf(false) }

            Box {
                FilterChip(
                    label = "Category: ${browseViewModel.selectedCategory}",
                    onClick = { categoryExpanded = true })

                DropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false })
                {
                    categories.forEach { category ->
                        DropdownMenuItem(text = { Text(category) }, onClick = {
                            browseViewModel.selectedCategory = category
                            categoryExpanded = false
                        })
                    }
                }
            }

            FilterChip(
                label = "Price: ₹${browseViewModel.priceRange.start.toInt()}-₹${browseViewModel.priceRange.endInclusive.toInt()}",
                onClick = { browseViewModel.showPriceFilterDialog = true })

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = "In stock", fontSize = 11.sp)
                Switch(
                    checked = browseViewModel.excludeOutOfStock,
                    onCheckedChange = { browseViewModel.excludeOutOfStock = it })
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (lazyPagingItems.loadState.refresh == LoadState.Loading) {
                items(10) {
                    ShimmerEffect(modifier = Modifier.aspectRatio(0.7f))
                }
            }

            items(lazyPagingItems.itemCount) { index ->
                lazyPagingItems[index]?.let { product ->
                    ProductCard(
                        product = product,
                        isSelected = browseViewModel.selectedProducts.contains(product),
                        onClick = {
                            if (browseViewModel.selectionMode) {
                                browseViewModel.onProductClicked(product)
                            } else {
                                val productJson = Gson().toJson(product)
                                val encodedUrl =
                                    URLEncoder.encode(
                                        productJson,
                                        StandardCharsets.UTF_8.toString()
                                    )
                                navController.navigate(Screen.ProductDetail.route + "/$encodedUrl")
                            }
                        },
                        onLongClick = { browseViewModel.onProductLongClicked(product) }
                    )
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
fun FilterChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(32.dp) // Reduced height
            .clip(RoundedCornerShape(8.dp))
            .background(Color.LightGray.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, fontSize = 11.sp)
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
        }
    }
}

@Composable
fun PriceFilterDialog(viewModel: BrowseViewModel) {
    Dialog(onDismissRequest = { viewModel.showPriceFilterDialog = false }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Price Range", fontSize = 20.sp)
                RangeSlider(
                    value = viewModel.priceRange,
                    onValueChange = { viewModel.priceRange = it },
                    valueRange = 0f..5000f,
                    steps = 100
                )
                Text(text = "₹${viewModel.priceRange.start.toInt()}-₹${viewModel.priceRange.endInclusive.toInt()}")
                Button(onClick = { viewModel.showPriceFilterDialog = false }) {
                    Text("Done")
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductCard(product: ApiProduct, isSelected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            SubcomposeAsyncImage(
                model = product.images.first().imageUrl,
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
                    text = "₹${product.price}",
                    color = Color.White
                )
                val outOfStock = !product.inStock
                if (outOfStock) {
                    Text(
                        text = "Out of stock",
                        color = Color.Red
                    )
                }
            }
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color.Transparent, shape = CircleShape)
                    )
                }
            }
        }
    }
}
