package com.kivoa.controlhub.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.kivoa.controlhub.data.RawProduct
import com.kivoa.controlhub.data.ProductDetailRequest // Assuming this is the correct import

data class ProductFormState(
    val rawImage: String,
    var mrp: String = "",
    var price: String = "",
    var discount: String = "",
    var gst: String = "",
    var purchaseMonth: String = "",
    var category: String = "Ring", // Default category
    var priceCode: String = "",
    var isRawImage: Boolean = false
) {
    val isValid: Boolean
        get() = mrp.isNotBlank() &&
                price.isNotBlank() &&
                discount.isNotBlank() &&
                gst.isNotBlank() &&
                purchaseMonth.isNotBlank() &&
                category.isNotBlank() &&
                priceCode.isNotBlank()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProductFormsDialog(
    selectedRawProducts: List<RawProduct>,
    onDismiss: () -> Unit,
    createViewModel: CreateViewModel,
    onProductCreationSuccess: () -> Unit
) {
    val initialProductForms = remember(selectedRawProducts) {
        mutableStateListOf(
            *selectedRawProducts.map { product ->
                ProductFormState(rawImage = product.imageUri)
            }.toTypedArray()
        )
    }

    val allFormsValid = remember(initialProductForms) {
        mutableStateOf(initialProductForms.all { it.isValid })
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Create Products",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(initialProductForms) { index, productFormState ->
                        ProductForm(
                            productFormState = productFormState,
                            onUpdateField = { updatedProductFormState ->
                                initialProductForms[index] = updatedProductFormState
                                allFormsValid.value = initialProductForms.all { it.isValid }
                            },
                            onValidationChange = {
                                allFormsValid.value = initialProductForms.all { it.isValid }
                            }
                        )
                    }
                    item { // Move the button inside LazyColumn
                        Button(
                            onClick = {
                                createViewModel.createProducts(initialProductForms) // This will need to be updated to pass `isRawImage`
                                onProductCreationSuccess()
                            },
                            enabled = allFormsValid.value,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Submit Products")
                        }
                    }
                    item { // Add a spacer after the button for padding at the bottom
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductForm(
    productFormState: ProductFormState,
    onUpdateField: (ProductFormState) -> Unit,
    onValidationChange: (Boolean) -> Unit
) {
    var mrpError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }
    var discountError by remember { mutableStateOf(false) }
    var gstError by remember { mutableStateOf(false) }
    var purchaseMonthError by remember { mutableStateOf(false) }
    var priceCodeError by remember { mutableStateOf(false) }
    var categoryError by remember { mutableStateOf(false) }


    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = rememberAsyncImagePainter(model = Uri.parse(productFormState.rawImage)),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = productFormState.mrp,
                    onValueChange = { newValue ->
                        mrpError = newValue.isBlank()
                        if (newValue.matches(Regex("""^\d*\.?\d*$"""))) {
                            onUpdateField(productFormState.copy(mrp = newValue))
                        }
                        onValidationChange(productFormState.isValid)
                    },
                    label = { Text("MRP") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    isError = mrpError,
                    supportingText = { if (mrpError) Text("Field cannot be empty") }
                )
                OutlinedTextField(
                    value = productFormState.price,
                    onValueChange = { newValue ->
                        priceError = newValue.isBlank()
                        if (newValue.matches(Regex("""^\d*\.?\d*$"""))) {
                            onUpdateField(productFormState.copy(price = newValue))
                        }
                        onValidationChange(productFormState.isValid)
                    },
                    label = { Text("Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    isError = priceError,
                    supportingText = { if (priceError) Text("Field cannot be empty") }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = productFormState.discount,
                    onValueChange = { newValue ->
                        discountError = newValue.isBlank()
                        if (newValue.matches(Regex("""^\d*\.?\d*$"""))) {
                            onUpdateField(productFormState.copy(discount = newValue))
                        }
                        onValidationChange(productFormState.isValid)
                    },
                    label = { Text("Discount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    isError = discountError,
                    supportingText = { if (discountError) Text("Field cannot be empty") }
                )
                OutlinedTextField(
                    value = productFormState.gst,
                    onValueChange = { newValue ->
                        gstError = newValue.isBlank()
                        if (newValue.matches(Regex("""^\d*\.?\d*$"""))) {
                            onUpdateField(productFormState.copy(gst = newValue))
                        }
                        onValidationChange(productFormState.isValid)
                    },
                    label = { Text("GST") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    isError = gstError,
                    supportingText = { if (gstError) Text("Field cannot be empty") }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = productFormState.purchaseMonth,
                onValueChange = { newValue ->
                    purchaseMonthError = newValue.isBlank()
                    if (newValue.length <= 4 && newValue.all { it.isDigit() }) {
                        onUpdateField(productFormState.copy(purchaseMonth = newValue))
                    }
                    onValidationChange(productFormState.isValid)
                },
                label = { Text("Purchase Month") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = purchaseMonthError,
                supportingText = { if (purchaseMonthError) Text("Field cannot be empty") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = productFormState.priceCode,
                onValueChange = { newValue ->
                    priceCodeError = newValue.isBlank()
                    onUpdateField(productFormState.copy(priceCode = newValue))
                    onValidationChange(productFormState.isValid)
                },
                label = { Text("Price Code") },
                modifier = Modifier.fillMaxWidth(),
                isError = priceCodeError,
                supportingText = { if (priceCodeError) Text("Field cannot be empty") }
            )
            Spacer(modifier = Modifier.height(8.dp))

            val categories = listOf("Ring", "Necklace", "Earring", "Bracelet")
            var expanded by remember { mutableStateOf(false) }
            var textFieldSize by remember { mutableStateOf(Size.Zero) }
            val icon =
                if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = productFormState.category,
                    onValueChange = {
                        // Category is read-only, validation is done on the current value
                        categoryError = productFormState.category.isBlank()
                        onValidationChange(productFormState.isValid)
                    },
                    readOnly = true,
                    label = { Text("Category")},
                    trailingIcon = {
                        Icon(
                            icon,
                            "contentDescription",
                            Modifier.clickable { expanded = !expanded })
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            textFieldSize = coordinates.size.toSize()
                        },
                    isError = categoryError,
                    supportingText = { if (categoryError) Text("Field cannot be empty") }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    categories.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                onUpdateField(productFormState.copy(category = selectionOption))
                                categoryError = selectionOption.isBlank()
                                expanded = false
                                onValidationChange(productFormState.isValid)
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Is Raw Image?")
                Switch(
                    checked = productFormState.isRawImage,
                    onCheckedChange = { isChecked ->
                        onUpdateField(productFormState.copy(isRawImage = isChecked))
                        onValidationChange(productFormState.isValid)
                    }
                )
            }
        }
    }
}
