package com.kivoa.controlhub.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.kivoa.controlhub.AppBarState
import com.kivoa.controlhub.AppBarViewModel
import com.kivoa.controlhub.data.Image
import com.kivoa.controlhub.data.ImagePriority
import com.kivoa.controlhub.data.ProductImage
import com.kivoa.controlhub.ui.components.shimmer
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

fun Image.toProductImage() = ProductImage(
    id = id,
    productId = productId,
    imageUrl = imageUrl,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt
)

@Composable
fun ReorderImagesScreen(
    productId: Long,
    navController: NavController,
    appBarViewModel: AppBarViewModel,
    productDetailViewModel: ProductDetailViewModel = viewModel()
) {
    val product by productDetailViewModel.product.collectAsState()
    var images by remember { mutableStateOf<List<ProductImage>>(emptyList()) }

    LaunchedEffect(productId) {
        productDetailViewModel.getProductById(productId)
    }

    LaunchedEffect(product) {
        product?.images?.let {
            images = it.map { image -> image.toProductImage() }
        }
    }

    val state = rememberReorderableLazyListState(onMove = { from, to ->
        images = images.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    })
    LaunchedEffect(Unit) {
        appBarViewModel.setAppBarState(
            AppBarState(
                title = { Text("Reorder Images") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        )
    }

    Column {
        LazyColumn(
            state = state.listState,
            modifier = Modifier
                .weight(1f)
                .reorderable(state)
        ) {
            itemsIndexed(images, { _, item -> item.id }) { index, item ->
                ReorderableItem(state, key = item.id) { isDragging ->
                    Row(
                        modifier = Modifier
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(item.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Product Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(100.dp),
                            loading = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .shimmer()
                                )
                            }
                        )
                        IconButton(
                            onClick = {},
                            modifier = Modifier.detectReorderAfterLongPress(state)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DragHandle,
                                contentDescription = "Reorder"
                            )
                        }
                    }
                }
            }
        }
        Button(
            onClick = {
                val priorities = images.mapIndexed { index, image ->
                    ImagePriority(image.id, index)
                }
                productDetailViewModel.updateProductImagePriorities(productId, priorities)
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("refresh", true)
                navController.popBackStack()
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Save")
        }
    }
}
