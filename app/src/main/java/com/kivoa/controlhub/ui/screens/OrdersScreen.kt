package com.kivoa.controlhub.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.google.gson.Gson
import com.kivoa.controlhub.AppBarState
import com.kivoa.controlhub.AppBarViewModel
import com.kivoa.controlhub.Screen
import com.kivoa.controlhub.data.shopify.order.Order

@Composable
fun OrdersScreen(
    navController: NavController,
    appBarViewModel: AppBarViewModel,
    ordersViewModel: OrdersViewModel
) {
    val orders = ordersViewModel.orders.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        appBarViewModel.setAppBarState(
            AppBarState(
                title = { Text("Orders") }
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (orders.loadState.refresh is LoadState.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(orders.itemCount) { index ->
                    orders[index]?.let { order ->
                        OrderItem(order = order, onClick = {
                            val orderJson = Gson().toJson(order)
                            navController.navigate(Screen.OrderDetail.withArgs(orderJson))
                        })
                    }
                }
                if (orders.loadState.append is LoadState.Loading) {
                    item {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderItem(order: Order, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            if (order.lineItems.size == 1) {
                order.lineItems[0].productImageUrl?.let { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Product Image",
                        modifier = Modifier
                            .size(100.dp)
                            .padding(end = 16.dp)
                    )
                }
            }
            Column {
                Text(text = "#${order.orderNumber}")
                Text(text = order.createdAt)
                Text(text = order.financialStatus)
                order.customer?.firstName?.let { firstName ->
                    order.customer.lastName?.let { lastName ->
                        Text(text = "Customer: $firstName $lastName")
                    }
                }
                if (order.lineItems.size == 1) {
                    order.lineItems[0].sku?.let { sku ->
                        Text(text = "SKU: $sku")
                    }
                }
            }
        }
    }
}