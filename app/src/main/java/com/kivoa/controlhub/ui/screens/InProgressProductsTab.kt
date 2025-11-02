package com.kivoa.controlhub.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.kivoa.controlhub.data.ApiProduct
import android.net.Uri

@Composable
fun InProgressProductsTab(
    inProgressProducts: List<ApiProduct>,
    isLoading: Boolean,
    onProductClick: (ApiProduct) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                Text("Loading in progress products...")
            }
            if (inProgressProducts.isEmpty() && !isLoading) {
                Text("No products in progress.", modifier = Modifier.padding(16.dp))
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(inProgressProducts) { product ->
                    InProgressProductItem(
                        product = product,
                        onClick = onProductClick
                    )
                }
            }
        }
    }
}

@Composable
fun InProgressProductItem(
    product: ApiProduct,
    onClick: (ApiProduct) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(product) },
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            val imageUrl = product.images.firstOrNull()?.imageUrl ?: product.rawImage
            Image(
                painter = rememberAsyncImagePainter(model = Uri.parse(imageUrl)),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = product.sku)
                Text(text = "Status: ${product.status}")
                Text(text = "Category: ${product.category}")
            }
        }
    }
}