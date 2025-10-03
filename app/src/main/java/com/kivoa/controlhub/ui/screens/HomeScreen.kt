package com.kivoa.controlhub.ui.screens

import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.kivoa.controlhub.R
import com.kivoa.controlhub.data.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = viewModel()
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val skuNumber by viewModel.skuNumber.collectAsState()
    val selectedPrefix by viewModel.selectedPrefix.collectAsState()
    val prefixes = viewModel.skuPrefixes

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.kivoa_logo),
            contentDescription = "Kivoa Logo",
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(16.dp))

        var expanded by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    modifier = Modifier.menuAnchor(),
                    readOnly = true,
                    value = selectedPrefix,
                    onValueChange = {},
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    prefixes.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                viewModel.onPrefixChange(selectionOption)
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = skuNumber,
                onValueChange = viewModel::onSkuNumberChange,
                label = { Text("SKU Number") },
                modifier = Modifier.weight(2f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isSearching) {
            CircularProgressIndicator()
        } else if (searchResults.isEmpty() && (skuNumber.length > 2)) {
            Text(text = "No results found")
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(searchResults) { product ->
                    ProductItem(product = product)
                }
            }
        }
    }
}

@Composable
fun ProductItem(product: Product) {
    var showZoomedImage by rememberSaveable { mutableStateOf(false) }

    if (showZoomedImage) {
        ZoomableImage(
            imageUrl = getGoogleDriveImageUrl(product.imageUrl),
            onDismiss = { showZoomedImage = false }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SubcomposeAsyncImage(
            model = getGoogleDriveImageUrl(product.imageUrl),
            loading = {
                ShimmerEffect(modifier = Modifier.size(96.dp))
            },
            contentDescription = null,
            modifier = Modifier
                .size(96.dp)
                .clickable { showZoomedImage = true }
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = "SKU: ${product.sku}")
            Text(text = "MRP: ${product.mrp}")
            Text(text = "Selling Price: ${product.sellingPrice}")
            Text(text = "Inventory: ${product.quantity}")
            Text(text = "Price Code: ${product.priceCode}")
        }
    }
}

@Composable
fun ShimmerEffect(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "shimmer-anim"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            Color.LightGray.copy(alpha = 0.9f),
            Color.LightGray.copy(alpha = 0.4f),
            Color.LightGray.copy(alpha = 0.9f)
        ),
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    Spacer(
        modifier = modifier
            .background(brush, shape = RoundedCornerShape(8.dp))
    )
}


@Composable
private fun ZoomableImage(imageUrl: String, onDismiss: () -> Unit) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale *= zoom
                        offset += pan
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = "Zoomed Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            )
        }
    }
}

private fun getGoogleDriveImageUrl(url: String): String {
    if (url.isBlank()) return ""
    val fileId = url.toUri().getQueryParameter("id")
    return if (fileId != null) {
        "https://drive.google.com/uc?export=download&id=$fileId"
    } else {
        url
    }
}
