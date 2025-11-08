package com.kivoa.controlhub.ui.screens

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.paging.compose.LazyPagingItems
import coil.compose.rememberAsyncImagePainter
import com.kivoa.controlhub.data.ApiRawImage
import kotlinx.collections.immutable.PersistentList

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PendingProductsTab(
    rawProducts: LazyPagingItems<ApiRawImage>,
    isLoading: Boolean,
    onImageClick: (Uri) -> Unit,
    selectedProductIds: PersistentList<Long>,
    onProductLongPress: (Long, Boolean) -> Unit,
    imagePickerLauncher: ActivityResultLauncher<String>
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                Text("Uploading images...")
            }
            if (rawProducts.itemCount == 0 && !isLoading) {
                Text("No pending products.", modifier = Modifier.padding(16.dp))
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(rawProducts.itemCount) { index ->
                    val product = rawProducts[index]
                    if (product != null) {
                        val isSelected = selectedProductIds.contains(product.id)
                        PendingProductItem(
                            product = product,
                            onImageClick = onImageClick,
                            isSelected = isSelected,
                            onLongPress = {
                                onProductLongPress(product.id, !isSelected)
                            }
                        )
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = {
                imagePickerLauncher.launch("image/*")
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, "Add new product images")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PendingProductItem(
    product: ApiRawImage,
    onImageClick: (Uri) -> Unit,
    isSelected: Boolean,
    onLongPress: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onImageClick(product.imageUrl.toUri()) },
                onLongClick = onLongPress
            ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box {
            Image(
                painter = rememberAsyncImagePainter(model = product.imageUrl.toUri()),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .size(128.dp),
                contentScale = ContentScale.Crop
            )
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