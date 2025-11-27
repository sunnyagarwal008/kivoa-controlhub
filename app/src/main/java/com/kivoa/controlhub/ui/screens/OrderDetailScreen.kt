package com.kivoa.controlhub.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.kivoa.controlhub.AppBarState
import com.kivoa.controlhub.AppBarViewModel
import com.kivoa.controlhub.Screen
import com.kivoa.controlhub.data.shopify.order.LineItem
import com.kivoa.controlhub.data.shopify.order.Order

@Composable
fun OrderDetailScreen(
    order: Order,
    navController: NavController,
    appBarViewModel: AppBarViewModel
) {
    LaunchedEffect(Unit) {
        appBarViewModel.setAppBarState(
            AppBarState(
                title = { Text("Order Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        )
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(text = "#${order.orderNumber}")
                Text(text = order.createdAt)
                Text(text = order.financialStatus)
                order.customer?.firstName?.let { firstName ->
                    order.customer.lastName?.let { lastName ->
                        Text(text = "Customer: $firstName $lastName")
                    }
                }
                order.shippingAddress.let { address ->
                    Text(text = "Address: ${address.address1}, ${address.city}, ${address.province} ${address.zip}, ${address.country}")
                }
            }
        }
        items(order.lineItems.size) { index ->
            order.lineItems[index].let { lineItem ->
                LineItemView(lineItem = lineItem, navController = navController)
            }
        }
    }
}

@Composable
fun LineItemView(lineItem: LineItem, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            lineItem.productImageUrl?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Product Image",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(end = 16.dp)
                        .clickable {
                            lineItem.productId?.let { productId ->
                                navController.navigate(Screen.ProductDetail.withArgs(productId))
                            }
                        }
                )
            }
            Column {
                Text(text = lineItem.title)
                lineItem.sku?.let { sku ->
                    Text(text = "SKU: $sku")
                }
                Text(text = "Price: ${lineItem.price}")
                Text(text = "Quantity: ${lineItem.quantity}")
            }
        }
    }
}