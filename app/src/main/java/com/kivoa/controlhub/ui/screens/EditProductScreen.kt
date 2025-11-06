package com.kivoa.controlhub.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kivoa.controlhub.api.RetrofitInstance
import com.kivoa.controlhub.data.ApiProduct
import com.kivoa.controlhub.data.UpdateProductRequest

@Composable
fun EditProductScreen(
    product: ApiProduct,
    navController: NavController,
    editProductViewModel: EditProductViewModel = viewModel(
        factory = EditProductViewModelFactory(RetrofitInstance.api)
    )
) {
    var category by remember { mutableStateOf(product.category) }
    var purchaseMonth by remember { mutableStateOf(product.purchaseMonth) }
    var mrp by remember { mutableStateOf(product.mrp.toString()) }
    var price by remember { mutableStateOf(product.price.toString()) }
    var discount by remember { mutableStateOf(product.discount.toString()) }
    var gst by remember { mutableStateOf(product.gst.toString()) }
    var priceCode by remember { mutableStateOf(product.priceCode ?: "") }
    var tags by remember { mutableStateOf(product.tags ?: "") }
    var boxNumber by remember { mutableStateOf(product.boxNumber?.toString() ?: "") }
    val updateState by editProductViewModel.updateState.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category") }
        )
        OutlinedTextField(
            value = purchaseMonth,
            onValueChange = { purchaseMonth = it },
            label = { Text("Purchase Month") }
        )
        OutlinedTextField(
            value = mrp,
            onValueChange = { mrp = it },
            label = { Text("MRP") }
        )
        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Price") }
        )
        OutlinedTextField(
            value = discount,
            onValueChange = { discount = it },
            label = { Text("Discount") }
        )
        OutlinedTextField(
            value = gst,
            onValueChange = { gst = it },
            label = { Text("GST") }
        )
        OutlinedTextField(
            value = priceCode,
            onValueChange = { priceCode = it },
            label = { Text("Price Code") }
        )
        OutlinedTextField(
            value = tags,
            onValueChange = { tags = it },
            label = { Text("Tags") }
        )
        OutlinedTextField(
            value = boxNumber,
            onValueChange = { boxNumber = it },
            label = { Text("Box Number") }
        )
        Button(onClick = {
            val request = UpdateProductRequest(
                category = category,
                purchaseMonth = purchaseMonth,
                mrp = mrp.toDouble(),
                price = price.toDouble(),
                discount = discount.toDouble(),
                gst = gst.toDouble(),
                priceCode = priceCode,
                tags = tags,
                boxNumber = boxNumber.toIntOrNull()
            )
            editProductViewModel.updateProduct(product.id, request)
        }) {
            Text("Update Product")
        }

        when (updateState) {
            is UpdateState.Success -> {
                navController.popBackStack()
            }
            is UpdateState.Error -> {
                Text("Error: ${(updateState as UpdateState.Error).message}")
            }
            else -> {}
        }
    }
}
