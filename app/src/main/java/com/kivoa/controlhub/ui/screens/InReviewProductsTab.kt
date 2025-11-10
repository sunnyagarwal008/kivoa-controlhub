package com.kivoa.controlhub.ui.screens

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.kivoa.controlhub.data.ApiProduct
import kotlinx.collections.immutable.PersistentList

@Composable
fun InReviewProductsTab(
    inReviewProducts: List<ApiProduct>,
    isLoading: Boolean,
    onProductClick: (ApiProduct) -> Unit,
    selectedProductIds: PersistentList<Long>,
    onProductLongPress: (Long, Boolean) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                Text("Loading in review products...")
            }
            if (inReviewProducts.isEmpty() && !isLoading) {
                Text("No products in review.", modifier = Modifier.padding(16.dp))
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(inReviewProducts) { product ->
                    val isSelected = selectedProductIds.contains(product.id)
                    InReviewProductItem(
                        product = product,
                        onClick = onProductClick,
                        isSelected = isSelected,
                        onLongPress = { productId, currentSelectionState ->
                            onProductLongPress(productId, !currentSelectionState)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InReviewProductItem(
    product: ApiProduct,
    onClick: (ApiProduct) -> Unit,
    isSelected: Boolean,
    onLongPress: (Long, Boolean) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick(product) },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress(product.id, isSelected)
                }
            ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box {
            val imageUrl = product.images.firstOrNull()?.imageUrl ?: product.rawImage
            Image(
                painter = rememberAsyncImagePainter(model = Uri.parse(imageUrl)),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                        startY = 0f,
                        endY = 150.dp.value // Adjust as needed to cover the text area
                    ))
                    .align(Alignment.BottomCenter) // Align the text box to the bottom
                    .padding(8.dp) // Add padding for the text
            ) {
                Column {
                    Text(text = product.sku, color = Color.White)
                }
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color.Green,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                )
            }
        }
    }
}