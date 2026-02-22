package com.kivoa.controlhub.ui.screens

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.kivoa.controlhub.AppBarState
import com.kivoa.controlhub.AppBarViewModel
import com.kivoa.controlhub.data.ApiProduct
import com.kivoa.controlhub.ui.components.shimmer
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Long,
    navController: NavController,
    shareViewModel: ShareViewModel,
    appBarViewModel: AppBarViewModel,
    productDetailViewModel: ProductDetailViewModel = viewModel()
) {
    var showZoomedImage by remember { mutableStateOf(false) }
    var showRawImage by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var showGenerateImageBottomSheet by remember { mutableStateOf(false) }
    var showDeleteImageConfirmationDialog by remember { mutableStateOf<Long?>(null) }
    var showShareBottomSheet by remember { mutableStateOf(false) }
    val product by productDetailViewModel.product.collectAsState()
    val isLoading by productDetailViewModel.isLoading.collectAsState()
    val productNotFound by productDetailViewModel.productNotFound.collectAsState()
    var currentImageIndex by remember { mutableIntStateOf(0) }
    val error by productDetailViewModel.error.collectAsState()
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showOrderSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            productDetailViewModel.uploadProductImage(productId, it, context)
        }
    }


    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            productDetailViewModel.clearError()
        }
    }


    val shouldRefreshState = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<Boolean>("refresh")
        ?.observeAsState()

    LaunchedEffect(shouldRefreshState?.value) {
        if (shouldRefreshState?.value == true) {
            productDetailViewModel.getProductById(productId)
            navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("refresh")
        }
    }
    LaunchedEffect(productId) {
        productDetailViewModel.getProductById(productId)
    }

    if (showShareBottomSheet) {
        product?.let {
            ShareBottomSheet(
                product = it,
                onDismiss = { showShareBottomSheet = false },
                shareViewModel = shareViewModel
            )
        }
    }

    LaunchedEffect(product) {
        product?.let {
            appBarViewModel.setAppBarState(
                AppBarState(
                    title = { Text("Product Detail") },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            navController.navigate("edit_product/${it.id}")
                        }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit"
                            )
                        }
                        IconButton(onClick = {
                            showShareBottomSheet = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share"
                            )
                        }
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More"
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (it.flagged) "Unflag" else "Flag") },
                                onClick = {
                                    productDetailViewModel.updateProductFlagged(it.id, !it.flagged)
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    showDeleteConfirmationDialog = true
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Generate Image") },
                                onClick = {
                                    showGenerateImageBottomSheet = true
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Upload Image") },
                                onClick = {
                                    imagePickerLauncher.launch("image/*")
                                    showMenu = false
                                }
                            )
                            if (it.images.size > 1) {
                                DropdownMenuItem(
                                    text = { Text("Reorder Images") },
                                    onClick = {
                                        navController.navigate("reorder_images/${it.id}")
                                        showMenu = false
                                    }
                                )
                            }
                            if (it.rawImage.isNotBlank()) {
                                DropdownMenuItem(
                                    text = { Text("View Raw Image") },
                                    onClick = {
                                        showRawImage = true
                                        showMenu = false
                                    }
                                )
                            }
                        }
                    }
                )
            )
        }
    }
    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text("Delete Product") },
            text = { Text("Are you sure you want to delete this product?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        productDetailViewModel.deleteProduct(productId)
                        navController.navigateUp()
                        showDeleteConfirmationDialog = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showGenerateImageBottomSheet) {
        GenerateImageBottomSheet(
            productDetailViewModel = productDetailViewModel,
            productId = productId,
            onDismiss = { showGenerateImageBottomSheet = false }
        )
    }

    showDeleteImageConfirmationDialog?.let { imageId ->
        AlertDialog(
            onDismissRequest = { showDeleteImageConfirmationDialog = null },
            title = { Text("Delete Image") },
            text = { Text("Are you sure you want to delete this image?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        productDetailViewModel.deleteProductImage(productId, imageId)
                        showDeleteImageConfirmationDialog = null
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteImageConfirmationDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }


    if (showZoomedImage) {
        ZoomableImageDialog(
            imageUrl = product?.images?.get(currentImageIndex)?.imageUrl ?: "",
            onDismiss = { showZoomedImage = false }
        )
    }

    if (showRawImage) {
        product?.rawImage?.let {
            RawImageDialog(
                imageUrl = it,
                onDismiss = { showRawImage = false }
            )
        }
    }

    if (productNotFound) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Product Not Found")
        }
    } else if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        product?.let { product ->
            val bottomSheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            )

            if (showOrderSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showOrderSheet = false },
                    sheetState = bottomSheetState
                ) {
                    PlaceOrderForm(
                        product = product,
                        productDetailViewModel = productDetailViewModel,
                        isLoading = isLoading,
                        onPlaceOrder = {
                            scope.launch {
                                bottomSheetState.hide()
                                showOrderSheet = false
                            }
                        }
                    )
                }
            }
            Scaffold { padding ->
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(padding)
                ) {
                    if (product.images.isNotEmpty()) {
                        val pagerState = rememberPagerState()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                        ) {
                            HorizontalPager(
                                count = product.images.size,
                                state = pagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                Box(modifier = Modifier.fillMaxSize()) {
                                    SubcomposeAsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(product.images[page].imageUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Product Image",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable {
                                                currentImageIndex = page
                                                showZoomedImage = true
                                            },
                                        loading = {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .shimmer()
                                            )
                                        }
                                    )
                                    if (product.images.size > 1) {
                                        IconButton(
                                            onClick = {
                                                showDeleteImageConfirmationDialog =
                                                    product.images[page].id
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete Image",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                            Icon(
                                imageVector = Icons.Filled.ZoomIn,
                                contentDescription = "Zoom In",
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                            )
                            HorizontalPagerIndicator(
                                pagerState = pagerState,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(16.dp),
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = product.title ?: "No Title",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = product.description ?: "No Description",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        ProductDetailRow(label = "SKU", value = product.sku)
                        ProductDetailRow(label = "Purchase Month", value = product.purchaseMonth)
                        ProductDetailRow(label = "Product Code", value = product.priceCode ?: "")

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Pricing",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ProductDetailRow(label = "MRP", value = "₹${product.mrp}")
                        ProductDetailRow(label = "Discount", value = "${product.discount}%")
                        ProductDetailRow(label = "Selling Price", value = "₹${product.price}")

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        product.tags?.let {
                            ProductDetailRow(label = "Tags", value = it)
                        }
                        product.boxNumber?.let {
                            ProductDetailRow(label = "Box Number", value = it.toString())
                        }
                        product.weight?.let {
                            ProductDetailRow(label = "Weight", value = "$it grams")
                        }
                        product.dimensions?.let {
                            val dimensions =
                                "${it.length} x ${it.breadth} x ${it.height} mm"
                            ProductDetailRow(label = "Dimensions", value = dimensions)
                        }
                        product.size?.let {
                            ProductDetailRow(label = "Size", value = it)
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        val outOfStock = !product.inStock
                        if (outOfStock) {
                            Text(
                                text = "Out of stock",
                                color = Color.Red,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    productDetailViewModel.updateProductStock(productId, true)
                                },
                                enabled = !isLoading,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text("Mark in Stock")
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showOrderSheet = true },
                                    enabled = !isLoading,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    } else {
                                        Text("Place Order")
                                    }
                                }
                                Button(
                                    onClick = {
                                        productDetailViewModel.updateProductStock(
                                            productId,
                                            false
                                        )
                                    },
                                    enabled = !isLoading,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    } else {
                                        Text("Mark out of Stock")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun PlaceOrderForm(
    product: ApiProduct,
    productDetailViewModel: ProductDetailViewModel,
    isLoading: Boolean,
    onPlaceOrder: () -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var address1 by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Panchkula") }
    var province by remember { mutableStateOf("Haryana") }
    var zip by remember { mutableStateOf("134107") }
    var shippingCharges by remember { mutableStateOf("0.0") }
    var orderPrice by remember { mutableStateOf(product.price.toString()) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Place Order", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        ProductDetailRow(label = "MRP", value = "₹${product.mrp}")
        ProductDetailRow(label = "Discount", value = "${product.discount}%")
        ProductDetailRow(label = "Selling Price", value = "₹${product.price}")
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = orderPrice,
                onValueChange = { orderPrice = it },
                label = { Text("Selling Price") },
                modifier = Modifier.weight(1f),
            )
            TextField(
                value = shippingCharges,
                onValueChange = { shippingCharges = it },
                label = { Text("Shipping Charges") },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = customerName,
            onValueChange = { customerName = it },
            label = { Text("Customer Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = customerPhone,
            onValueChange = { customerPhone = it },
            label = { Text("Customer Phone") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = address1,
            onValueChange = { address1 = it },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") },
                modifier = Modifier.weight(1f)
            )
            TextField(
                value = province,
                onValueChange = { province = it },
                label = { Text("State") },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = zip,
            onValueChange = { zip = it },
            label = { Text("Pincode") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                productDetailViewModel.placeOrder(
                    customerName = customerName,
                    customerPhone = customerPhone,
                    address1 = address1,
                    city = city,
                    province = province,
                    zip = zip,
                    shippingCharges = shippingCharges.toDoubleOrNull() ?: 0.0,
                    perUnitPrice = orderPrice.toDoubleOrNull() ?: 0.0,
                    onSuccess = {
                        Toast
                            .makeText(context, "Order placed successfully", Toast.LENGTH_SHORT)
                            .show()
                        onPlaceOrder()
                    },
                    onError = {
                        Toast
                            .makeText(context, it, Toast.LENGTH_SHORT)
                            .show()
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Submit")
            }
        }
    }
}

@Composable
fun RawImageDialog(imageUrl: String, onDismiss: () -> Unit) {
    var finalImageUrl = imageUrl
    if (imageUrl.contains("drive.google.com")) {
        val fileId = if (imageUrl.contains("/d/")) {
            imageUrl.substringAfter("/d/").substringBefore("/view")
        } else {
            imageUrl.substringAfter("id=").substringBefore("&")
        }
        finalImageUrl = "https://drive.google.com/uc?export=download&id=$fileId"
    }
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Box(modifier = Modifier.fillMaxSize()) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(finalImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Raw Product Image",
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .shimmer()
                    )
                }
            )
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                IconButton(onClick = { downloadImage(context, finalImageUrl) }) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = "Download Image",
                        tint = Color.White
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateImageBottomSheet(
    productDetailViewModel: ProductDetailViewModel,
    productId: Long,
    onDismiss: () -> Unit
) {
    val prompts by productDetailViewModel.prompts.collectAsState()
    val product by productDetailViewModel.product.collectAsState()
    var isGenerating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedPromptType by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(product) {
        product?.category?.let { productDetailViewModel.getPrompts(it) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isGenerating) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
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
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Select"
                                )
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = DividerDefaults.color)
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
                                        scope.launch {
                                            isGenerating = true
                                            val success =
                                                productDetailViewModel.generateProductImage(
                                                    productId,
                                                    prompt.id
                                                )
                                            if (success) {
                                                Toast
                                                    .makeText(
                                                        context,
                                                        "Image generated successfully",
                                                        Toast.LENGTH_SHORT
                                                    )
                                                    .show()
                                                onDismiss()
                                            }
                                            isGenerating = false
                                        }
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
                            HorizontalDivider(thickness = 0.5.dp, color = DividerDefaults.color)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ZoomableAsyncImage(
    model: ImageRequest,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale *= zoom
                    offset += pan
                }
            }
    ) {
        SubcomposeAsyncImage(
            model = model,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                ),
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shimmer()
                )
            }
        )
    }
}

@Composable
fun ZoomableImageDialog(imageUrl: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Box(modifier = Modifier.fillMaxSize()) {
            ZoomableAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Product Image",
            )
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                IconButton(onClick = { downloadImage(context, imageUrl) }) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = "Download Image",
                        tint = Color.White
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

fun downloadImage(context: Context, url: String) {
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val request = DownloadManager.Request(Uri.parse(url))
        .setTitle("Image Download")
        .setDescription("Downloading")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${System.currentTimeMillis()}.jpg")
    downloadManager.enqueue(request)
    Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(
    product: ApiProduct,
    onDismiss: () -> Unit,
    shareViewModel: ShareViewModel
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    var isGeneratingGif by remember { mutableStateOf(false) }
    var shareTitle by remember { mutableStateOf(true) }
    var shareDescription by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableIntStateOf(0) }


    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Share", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { shareTitle = !shareTitle }
            ) {
                Checkbox(checked = shareTitle, onCheckedChange = { shareTitle = it })
                Text("Share Title")
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { shareDescription = !shareDescription }
            ) {
                Checkbox(checked = shareDescription, onCheckedChange = { shareDescription = it })
                Text("Share Description")
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (isGeneratingGif) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generating GIF...")
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(product.images.size) { index ->
                        val image = product.images[index]
                        val isSelected = selectedImageIndex == index
                        val borderModifier = if (isSelected) {
                            Modifier.border(2.dp, MaterialTheme.colorScheme.primary)
                        } else {
                            Modifier
                        }

                        SubcomposeAsyncImage(
                            model = image.imageUrl,
                            contentDescription = "Product Image",
                            modifier = Modifier
                                .size(100.dp)
                                .then(borderModifier)
                                .clickable {
                                    selectedImageIndex = index
                                },
                            loading = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .shimmer()
                                )
                            }
                        )
                    }
                    if (product.images.size > 2) {
                        item {
                            Column(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clickable {
                                        isGeneratingGif = true
                                        shareViewModel.shareProductAsGif(
                                            product,
                                            context,
                                            shareTitle,
                                            shareDescription
                                        ) {
                                            isGeneratingGif = false
                                            onDismiss()
                                        }
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Gif, contentDescription = "Share GIF")
                                Text("Share as GIF")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        shareViewModel.shareProductImage(
                            product,
                            product.images[selectedImageIndex].imageUrl,
                            context,
                            shareTitle,
                            shareDescription
                        )
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Share Image")
                }
            }
        }
    }
}