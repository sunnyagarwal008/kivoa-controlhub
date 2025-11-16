package com.kivoa.controlhub.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import com.kivoa.controlhub.data.Prompt
import com.kivoa.controlhub.data.RawProduct
import java.math.BigDecimal
import java.math.RoundingMode

data class ProductFormState(
    val rawImage: String,
    var mrp: String = "",
    var price: String = "",
    var discount: String = "0",
    var gst: String = "3",
    var purchaseMonth: String = "",
    var category: String = "", // Default category
    var priceCode: String = "",
    var isRawImage: Boolean = false,
    var boxNumber: String = "",
    var tags: List<String> = emptyList(),
    var promptId: Long? = null,
    var promptText: String = ""
) {
    val isValid: Boolean
        get() = mrp.isNotBlank() &&
                discount.isNotBlank() &&
                gst.isNotBlank() &&
                purchaseMonth.isNotBlank() &&
                category.isNotBlank() &&
                priceCode.isNotBlank() &&
                (!isRawImage || promptId != null)
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
            val bottomPadding = with(LocalDensity.current) {
                WindowInsets.navigationBars.getBottom(this).toDp()
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Create Products",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = bottomPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            },
                            createViewModel = createViewModel
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
                        Spacer(modifier = Modifier.height(8.dp))
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
    onValidationChange: (Boolean) -> Unit,
    createViewModel: CreateViewModel
) {
    var mrpError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }
    var discountError by remember { mutableStateOf(false) }
    var gstError by remember { mutableStateOf(false) }
    var purchaseMonthError by remember { mutableStateOf(false) }
    var priceCodeError by remember { mutableStateOf(false) }
    var categoryError by remember { mutableStateOf(false) }
    var boxNumberError by remember { mutableStateOf(false) }
    val categories by createViewModel.categories.collectAsState()
    val prompts by createViewModel.prompts.collectAsState()


    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp)) {
            Image(
                painter = rememberAsyncImagePainter(model = productFormState.rawImage.toUri()),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = productFormState.mrp,
                    onValueChange = { newValue ->
                        mrpError = newValue.isBlank()
                        if (newValue.matches(Regex("""^\d*\.?\d*$"""))) {
                            val mrp = newValue.toBigDecimalOrNull()
                            val discount = productFormState.discount.toBigDecimalOrNull()
                            if (mrp != null && discount != null) {
                                val sellingPrice = mrp - (mrp * discount.divide(BigDecimal(100)))
                                onUpdateField(
                                    productFormState.copy(
                                        mrp = newValue,
                                        price = sellingPrice.setScale(2, RoundingMode.HALF_UP).toPlainString()
                                    )
                                )
                            } else {
                                onUpdateField(productFormState.copy(mrp = newValue))
                            }
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
                    value = productFormState.discount,
                    onValueChange = { newValue ->
                        discountError = newValue.isBlank()
                        if (newValue.matches(Regex("""^\d*\.?\d*$"""))) {
                            val mrp = productFormState.mrp.toBigDecimalOrNull()
                            val discount = newValue.toBigDecimalOrNull()
                            if (mrp != null && discount != null) {
                                val sellingPrice = mrp - (mrp * discount.divide(BigDecimal(100)))
                                onUpdateField(
                                    productFormState.copy(
                                        discount = newValue,
                                        price = sellingPrice.setScale(2, RoundingMode.HALF_UP).toPlainString()
                                    )
                                )
                            } else {
                                onUpdateField(productFormState.copy(discount = newValue))
                            }
                        }
                        onValidationChange(productFormState.isValid)
                    },
                    label = { Text("Discount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    isError = discountError,
                    supportingText = { if (discountError) Text("Field cannot be empty") }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = productFormState.price,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    isError = priceError,
                    supportingText = { if (priceError) Text("Field cannot be empty") }
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
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
                    modifier = Modifier.weight(1f),
                    isError = purchaseMonthError,
                    supportingText = { if (purchaseMonthError) Text("Field cannot be empty") }
                )
                OutlinedTextField(
                    value = productFormState.priceCode,
                    onValueChange = { newValue ->
                        priceCodeError = newValue.isBlank()
                        onUpdateField(productFormState.copy(priceCode = newValue))
                        onValidationChange(productFormState.isValid)
                    },
                    label = { Text("Price Code") },
                    modifier = Modifier.weight(1f),
                    isError = priceCodeError,
                    supportingText = { if (priceCodeError) Text("Field cannot be empty") }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                var expanded by remember { mutableStateOf(false) }
                val icon =
                    if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = productFormState.category,
                        onValueChange = {
                            // Category is read-only, validation is done on the current value
                            categoryError = productFormState.category.isBlank()
                            onValidationChange(productFormState.isValid)
                        },
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            Icon(
                                icon,
                                "contentDescription",
                                Modifier.clickable { expanded = !expanded })
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        isError = categoryError,
                        supportingText = { if (categoryError) Text("Field cannot be empty") }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        categories.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption.name) },
                                onClick = {
                                    onUpdateField(productFormState.copy(category = selectionOption.name))
                                    categoryError = selectionOption.name.isBlank()
                                    expanded = false
                                    onValidationChange(productFormState.isValid)
                                    createViewModel.fetchPrompts(selectionOption.name)
                                }
                            )
                        }
                    }
                }
                // Tags multiselect dropdown (placeholder)
                // In a real app, you'd fetch these from your view model
                val allTags =
                    categories.find { it.name == productFormState.category }?.tags?.split(",")
                        ?: emptyList()
                var tagsExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = tagsExpanded,
                    onExpandedChange = { tagsExpanded = !tagsExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = productFormState.tags.joinToString(", "),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tags") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                "contentDescription",
                                Modifier.clickable { tagsExpanded = !tagsExpanded })
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = tagsExpanded,
                        onDismissRequest = { tagsExpanded = false }
                    ) {
                        allTags.forEach { tag ->
                            DropdownMenuItem(
                                text = { Text(tag) },
                                onClick = {
                                    val currentTags = productFormState.tags.toMutableList()
                                    if (currentTags.contains(tag)) {
                                        currentTags.remove(tag)
                                    } else {
                                        currentTags.add(tag)
                                    }
                                    onUpdateField(productFormState.copy(tags = currentTags))
                                    onValidationChange(productFormState.isValid)
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedTextField(
                    value = productFormState.boxNumber,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            onUpdateField(productFormState.copy(boxNumber = newValue))
                        }
                        onValidationChange(productFormState.isValid)
                    },
                    label = { Text("Box Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    isError = boxNumberError,
                    supportingText = { if (boxNumberError) Text("Field cannot be empty") }
                )
                Row(
                    modifier = Modifier.weight(1f),
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
            if (productFormState.isRawImage) {
                Spacer(modifier = Modifier.height(4.dp))
                var showPromptSheet by remember { mutableStateOf(false) }

                if (showPromptSheet) {
                    PromptSelectionBottomSheet(
                        prompts = prompts,
                        onDismiss = { showPromptSheet = false },
                        onPromptSelected = { prompt ->
                            onUpdateField(
                                productFormState.copy(
                                    promptId = prompt.id,
                                    promptText = prompt.text
                                )
                            )
                            showPromptSheet = false
                            onValidationChange(productFormState.isValid)
                        }
                    )
                }
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPromptSheet = true }) {
                    OutlinedTextField(
                        value = productFormState.promptText.ifEmpty { "Click to select a prompt" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Prompt") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            // For Icons
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptSelectionBottomSheet(
    prompts: List<Prompt>,
    onDismiss: () -> Unit,
    onPromptSelected: (Prompt) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedPromptType by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (selectedPromptType == null) {
                Text("Select a prompt type", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                val promptTypes = prompts.mapNotNull { it.type }.distinct()
                LazyColumn {
                    items(promptTypes) { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPromptType = type }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = type,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Select"
                            )
                        }
                        HorizontalDivider()
                    }
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedPromptType = null }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Text("Select a prompt", style = MaterialTheme.typography.titleLarge)
                }
                Spacer(modifier = Modifier.height(16.dp))
                val promptsForType = prompts.filter { it.type == selectedPromptType }
                LazyColumn {
                    items(promptsForType) { prompt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onPromptSelected(prompt)
                                    onDismiss()
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = prompt.text,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
