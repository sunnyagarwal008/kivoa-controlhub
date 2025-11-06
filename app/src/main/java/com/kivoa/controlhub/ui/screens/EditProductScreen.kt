package com.kivoa.controlhub.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kivoa.controlhub.api.RetrofitInstance
import com.kivoa.controlhub.data.UpdateProductRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    productId: Long,
    navController: NavController,
    editProductViewModel: EditProductViewModel = viewModel(
        factory = EditProductViewModelFactory(RetrofitInstance.api)
    )
) {
    val product by editProductViewModel.product.collectAsState()
    val updateState by editProductViewModel.updateState.collectAsState()

    LaunchedEffect(productId) {
        editProductViewModel.getProductById(productId)
    }

    if (product == null) {
        CircularProgressIndicator()
    } else {
        var category by remember { mutableStateOf(product!!.category) }
        var purchaseMonth by remember { mutableStateOf(product!!.purchaseMonth) }
        var mrp by remember { mutableStateOf(product!!.mrp.toString()) }
        var price by remember { mutableStateOf(product!!.price.toString()) }
        var discount by remember { mutableStateOf(product!!.discount.toString()) }
        var gst by remember { mutableStateOf(product!!.gst.toString()) }
        var priceCode by remember { mutableStateOf(product!!.priceCode ?: "") }
        val categoryTags = remember(product!!.categoryDetails.tags) {
            product!!.categoryDetails.tags.split(",").map { it.trim() }
        }
        var selectedTags by remember {
            mutableStateOf(
                product!!.tags?.split(",")?.map { it.trim() }?.toSet() ?: emptySet()
            )
        }
        var boxNumber by remember { mutableStateOf(product!!.boxNumber?.toString() ?: "") }
        var expanded by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = purchaseMonth,
                onValueChange = { purchaseMonth = it },
                label = { Text("Purchase Month") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = mrp,
                    onValueChange = { mrp = it },
                    label = { Text("MRP") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = discount,
                    onValueChange = { discount = it },
                    label = { Text("Discount") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextField(
                    value = gst,
                    onValueChange = { gst = it },
                    label = { Text("GST") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = priceCode,
                    onValueChange = { priceCode = it },
                    label = { Text("Price Code") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextField(
                    value = boxNumber,
                    onValueChange = { boxNumber = it },
                    label = { Text("Box Number") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedTags.joinToString(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tags") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categoryTags.forEach { tag ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = tag in selectedTags,
                                        onCheckedChange = {
                                            selectedTags = if (it) {
                                                selectedTags + tag
                                            } else {
                                                selectedTags - tag
                                            }
                                        }
                                    )
                                    Text(text = tag)
                                }
                            },
                            onClick = {
                                selectedTags = if (tag in selectedTags) {
                                    selectedTags - tag
                                } else {
                                    selectedTags + tag
                                }
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val request = UpdateProductRequest(
                        category = category,
                        purchaseMonth = purchaseMonth,
                        mrp = mrp.toDouble(),
                        price = price.toDouble(),
                        discount = discount.toDouble(),
                        gst = gst.toDouble(),
                        priceCode = priceCode,
                        tags = selectedTags.joinToString(","),
                        boxNumber = boxNumber.toIntOrNull()
                    )
                    editProductViewModel.updateProduct(product!!.id, request)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Update Product")
            }

            when (updateState) {
                is UpdateState.Success -> {
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                    navController.popBackStack()
                }

                is UpdateState.Error -> {
                    Text("Error: ${(updateState as UpdateState.Error).message}")
                }

                else -> {}
            }
        }
    }
}