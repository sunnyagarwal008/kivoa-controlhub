package com.kivoa.controlhub.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete // Import the Delete icon
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.kivoa.controlhub.data.RawProduct
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import com.kivoa.controlhub.AppBarState
import com.kivoa.controlhub.AppBarViewModel
import androidx.compose.runtime.LaunchedEffect
import com.kivoa.controlhub.data.ApiProduct
import androidx.compose.material.icons.filled.Check


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CreateScreen(
    createViewModel: CreateViewModel = viewModel(),
    appBarViewModel: AppBarViewModel
) {
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Pending", "In Progress", "In Review") // Added "In Progress"

    val rawProducts by createViewModel.rawProducts.collectAsState()
    val isLoading by createViewModel.isLoading.collectAsState()
    val bulkProductCreationSuccess by createViewModel.bulkProductCreationSuccess.collectAsState()
    val inReviewProducts by createViewModel.inReviewProducts.collectAsState()
    val inReviewProductsLoading by createViewModel.inReviewProductsLoading.collectAsState()
    val inProgressProducts by createViewModel.inProgressProducts.collectAsState() // Collect in progress products
    val inProgressProductsLoading by createViewModel.inProgressProductsLoading.collectAsState() // Collect in progress loading state
    val selectedInReviewProductIds by createViewModel.selectedInReviewProductIds.collectAsState()

    val context = LocalContext.current

    var showFullScreenImageDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedProductUris by remember { mutableStateOf<PersistentList<Uri>>(persistentListOf()) }
    var showCreateProductFormsDialog by remember { mutableStateOf(false) }

    // Observe bulkProductCreationSuccess to dismiss dialog and clear selections
    if (bulkProductCreationSuccess && showCreateProductFormsDialog) {
        showCreateProductFormsDialog = false
        selectedProductUris = persistentListOf()
        // Reset the success state in ViewModel after handling it
        createViewModel.resetBulkProductCreationSuccess()
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        createViewModel.onImagesSelected(uris, context)
    }

    LaunchedEffect(tabIndex) {
        when (tabIndex) {
            1 -> createViewModel.fetchInProgressProducts() // Fetch in progress products
            2 -> createViewModel.fetchInReviewProducts()
        }
    }

    LaunchedEffect(selectedProductUris.isNotEmpty(), selectedInReviewProductIds.isNotEmpty(), tabIndex) {
        val currentAppBarState = when (tabIndex) {
            0 -> {
                if (selectedProductUris.isNotEmpty()) {
                    AppBarState(
                        title = { Text("Selected ${selectedProductUris.size} images") },
                        navigationIcon = {
                            IconButton(onClick = { selectedProductUris = persistentListOf() }) {
                                Icon(Icons.Default.Close, "Clear selection")
                            }
                        },
                        actions = {
                            IconButton(onClick = { 
                                createViewModel.deleteRawProducts(selectedProductUris.toList())
                                selectedProductUris = persistentListOf()
                            }) {
                                Icon(Icons.Default.Delete, "Delete Products")
                            }
                            IconButton(onClick = { showCreateProductFormsDialog = true }) {
                                Icon(Icons.Default.Done, "Create Products")
                            }
                        }
                    )
                } else {
                    AppBarState(title = { Text(tabs[tabIndex]) })
                }
            }
            2 -> {
                if (selectedInReviewProductIds.isNotEmpty()) {
                    AppBarState(
                        title = { Text("Selected ${selectedInReviewProductIds.size} products") },
                        navigationIcon = {
                            IconButton(onClick = { createViewModel.clearSelectedInReviewProductIds() }) {
                                Icon(Icons.Default.Close, "Clear selection")
                            }
                        },
                        actions = {
                            IconButton(onClick = { 
                                createViewModel.updateProductsStatus(selectedInReviewProductIds.toList(), "live")
                            }) {
                                Icon(Icons.Default.Check, "Mark as Live")
                            }
                        }
                    )
                } else {
                    AppBarState(title = { Text(tabs[tabIndex]) })
                }
            }
            else -> AppBarState(title = { Text(tabs[tabIndex]) })
        }
        appBarViewModel.setAppBarState(currentAppBarState)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = tabIndex == index,
                    onClick = { tabIndex = index }
                )
            }
        }

        when (tabIndex) {
            0 -> { // Content for "Pending" tab
                PendingProductsTab(
                    rawProducts = rawProducts,
                    isLoading = isLoading,
                    onImageClick = { uri ->
                        selectedImageUri = uri
                        showFullScreenImageDialog = true
                    },
                    selectedProductUris = selectedProductUris,
                    onProductLongPress = { uri, isSelected ->
                        selectedProductUris = if (isSelected) {
                            selectedProductUris.add(uri)
                        } else {
                            selectedProductUris.remove(uri)
                        }
                    },
                    imagePickerLauncher = imagePickerLauncher // Pass the launcher
                )
            }

            1 -> { // Content for "In Progress" tab
                InProgressProductsTab(
                    inProgressProducts = inProgressProducts,
                    isLoading = inProgressProductsLoading,
                    onProductClick = { product ->
                        selectedImageUri = Uri.parse(product.images.firstOrNull()?.imageUrl ?: product.rawImage)
                        showFullScreenImageDialog = true
                    }
                )
            }

            2 -> { // Content for "In Review" tab
                InReviewProductsTab(
                    inReviewProducts = inReviewProducts,
                    isLoading = inReviewProductsLoading,
                    onProductClick = { product ->
                        selectedImageUri = Uri.parse(product.images.firstOrNull()?.imageUrl ?: product.rawImage)
                        showFullScreenImageDialog = true
                    },
                    selectedProductIds = selectedInReviewProductIds,
                    onProductLongPress = { productId, isSelected ->
                        createViewModel.updateSelectedInReviewProductIds(productId, isSelected)
                    }
                )
            }
        }
    }

    if (showFullScreenImageDialog && selectedImageUri != null) {
        FullScreenImageDialog(
            imageUri = selectedImageUri!!,
            onDismiss = { showFullScreenImageDialog = false }
        )
    }

    if (showCreateProductFormsDialog) {
        val selectedRawProducts = rawProducts.filter { product ->
            selectedProductUris.contains(Uri.parse(product.imageUri))
        }
        CreateProductFormsDialog(
            selectedRawProducts = selectedRawProducts,
            onDismiss = { showCreateProductFormsDialog = false },
            createViewModel = createViewModel,
            onProductCreationSuccess = { // This lambda is called when products are successfully created
                showCreateProductFormsDialog = false
                selectedProductUris = persistentListOf()
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PendingProductsTab(
    rawProducts: List<RawProduct>,
    isLoading: Boolean,
    onImageClick: (Uri) -> Unit,
    selectedProductUris: PersistentList<Uri>,
    onProductLongPress: (Uri, Boolean) -> Unit,
    imagePickerLauncher: androidx.activity.result.ActivityResultLauncher<String> // Added imagePickerLauncher
) {
    Box(modifier = Modifier.fillMaxSize()) { // Used Box to stack content and FAB
        Column(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                Text("Uploading images...")
            }
            if (rawProducts.isEmpty() && !isLoading) {
                Text("No pending products.", modifier = Modifier.padding(16.dp))
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(rawProducts) { product ->
                    val uri = Uri.parse(product.imageUri)
                    val isSelected = selectedProductUris.contains(uri)
                    PendingProductItem(
                        product = product,
                        onImageClick = onImageClick,
                        isSelected = isSelected,
                        onLongPress = {
                            onProductLongPress(uri, !isSelected)
                        }
                    )
                }
            }
        }
        FloatingActionButton( // Moved FAB inside PendingProductsTab
            onClick = {
                imagePickerLauncher.launch("image/*")
            },
            modifier = Modifier
                .align(Alignment.BottomEnd) // Aligned to bottom end
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, "Add new product images")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PendingProductItem(
    product: RawProduct,
    onImageClick: (Uri) -> Unit,
    isSelected: Boolean,
    onLongPress: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onImageClick(Uri.parse(product.imageUri)) },
                onLongClick = onLongPress
            ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box {
            Image(
                painter = rememberAsyncImagePainter(model = Uri.parse(product.imageUri)),
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

@Composable
fun FullScreenImageDialog(imageUri: Uri, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = rememberAsyncImagePainter(model = imageUri),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

data class ProductFormState(
    val rawImage: String,
    var mrp: String = "",
    var price: String = "",
    var discount: String = "",
    var gst: String = "",
    var purchaseMonth: String = "",
    var category: String = "Ring" // Default category
)

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
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        createViewModel.createProducts(initialProductForms)
                        onProductCreationSuccess()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Submit Products")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductForm(productFormState: ProductFormState, onUpdateField: (ProductFormState) -> Unit) {
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
            OutlinedTextField(
                value = productFormState.mrp,
                onValueChange = { newValue ->
                    if (newValue.matches(Regex("""^\d*\.?\d*$"""))) {
                        onUpdateField(productFormState.copy(mrp = newValue))
                    }
                },
                label = { Text("MRP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = productFormState.price,
                onValueChange = { newValue ->
                    if (newValue.matches(Regex("""^\d*\.?\d*$"""))) {
                        onUpdateField(productFormState.copy(price = newValue))
                    }
                },
                label = { Text("Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = productFormState.discount,
                onValueChange = { newValue ->
                    if (newValue.matches(Regex("""^\d*\.?\d*$"""))) {
                        onUpdateField(productFormState.copy(discount = newValue))
                    }
                },
                label = { Text("Discount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = productFormState.gst,
                onValueChange = { newValue ->
                    if (newValue.matches(Regex("""^\d*\.?\d*$"""))) {
                        onUpdateField(productFormState.copy(gst = newValue))
                    }
                },
                label = { Text("GST") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = productFormState.purchaseMonth,
                onValueChange = { newValue ->
                    if (newValue.length <= 4 && newValue.all { it.isDigit() }) {
                        onUpdateField(productFormState.copy(purchaseMonth = newValue))
                    }
                },
                label = { Text("Purchase Month (MMyy)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
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
                    onValueChange = { },
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
                        }
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
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InReviewProductsTab(
    inReviewProducts: List<ApiProduct>,
    isLoading: Boolean,
    onProductClick: (ApiProduct) -> Unit,
    selectedProductIds: PersistentList<Long>, // Changed to Long
    onProductLongPress: (Long, Boolean) -> Unit // Changed to Long
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                Text("Loading in review products...")
            }
            if (inReviewProducts.isEmpty() && !isLoading) {
                Text("No products in review.", modifier = Modifier.padding(16.dp))
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(inReviewProducts) { product ->
                    val isSelected = selectedProductIds.contains(product.id)
                    InReviewProductItem(
                        product = product,
                        onClick = onProductClick,
                        isSelected = isSelected,
                        onLongPress = { productId, currentSelectionState ->
                            onProductLongPress(productId, !currentSelectionState)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InReviewProductItem(
    product: ApiProduct,
    onClick: (ApiProduct) -> Unit,
    isSelected: Boolean,
    onLongPress: (Long, Boolean) -> Unit // Changed to Long
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick(product) },
                onLongClick = { onLongPress(product.id, isSelected) }
            ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box {
            val imageUrl = product.images.firstOrNull()?.imageUrl ?: product.rawImage
            Image(
                painter = rememberAsyncImagePainter(model = imageUrl),
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
                painter = rememberAsyncImagePainter(model = imageUrl),
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