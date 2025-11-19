package com.kivoa.controlhub.ui.screens

import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.SubcomposeAsyncImage
import com.kivoa.controlhub.AppBarState
import com.kivoa.controlhub.AppBarViewModel
import com.kivoa.controlhub.Screen
import com.kivoa.controlhub.ShimmerEffect
import com.kivoa.controlhub.api.RetrofitInstance
import com.kivoa.controlhub.data.ApiProduct
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalMaterialApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun ProductsScreen(
    productsViewModel: ProductsViewModel = viewModel(),
    navController: NavController,
    appBarViewModel: AppBarViewModel,
    searchViewModel: SearchViewModel = viewModel()
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val apiService = RetrofitInstance.api

    val lazyPagingItems = productsViewModel.products.collectAsLazyPagingItems()
    val shareViewModelFactory =
        remember {
            ShareViewModelFactory(application)
        }
    val shareViewModel: ShareViewModel = viewModel(factory = shareViewModelFactory)

    val categories by productsViewModel.categories.collectAsState()
    val tags by productsViewModel.tags.collectAsState()
    val filterParams by productsViewModel.filterParams.collectAsState()
    val pdfCatalogUrl by productsViewModel.pdfCatalogUrl.collectAsState()
    val discountAppliedMessage by productsViewModel.discountAppliedMessage.collectAsState()
    val totalProducts by productsViewModel.totalProducts.collectAsState()

    var showPdfNameDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showSearchSheet by remember { mutableStateOf(false) }


    LaunchedEffect(productsViewModel.selectionMode, productsViewModel.selectedProducts.size, totalProducts) {
        appBarViewModel.setAppBarState(
            AppBarState(
                title = {
                    if (productsViewModel.selectionMode) {
                        Text("${productsViewModel.selectedProducts.size} selected")
                    } else {
                        Column {
                            Text(Screen.Products.route)
                            if (totalProducts > 0) {
                                Text(
                                    "$totalProducts products",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    if (productsViewModel.selectionMode) {
                        IconButton(onClick = {
                            productsViewModel.selectionMode = false
                            productsViewModel.selectedProducts = emptySet()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    if (productsViewModel.selectionMode) {
                        IconButton(onClick = { shareViewModel.shareProducts(productsViewModel.selectedProducts) }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                    } else {
                        IconButton(onClick = { showSearchSheet = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        var sortExpanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { sortExpanded = true }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = "Sort options"
                                )
                            }
                            DropdownMenu(
                                expanded = sortExpanded,
                                onDismissRequest = { sortExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("SKU (Ascending)") },
                                    onClick = {
                                        productsViewModel.updateSort("sku_sequence_number", "asc")
                                        sortExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("SKU (Descending)") },
                                    onClick = {
                                        productsViewModel.updateSort("sku_sequence_number", "desc")
                                        sortExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Price (Ascending)") },
                                    onClick = {
                                        productsViewModel.updateSort("price", "asc")
                                        sortExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Price (Descending)") },
                                    onClick = {
                                        productsViewModel.updateSort("price", "desc")
                                        sortExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Created At (Newest First)") },
                                    onClick = {
                                        productsViewModel.updateSort("created_at", "desc")
                                        sortExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Created At (Oldest First)") },
                                    onClick = {
                                        productsViewModel.updateSort("created_at", "asc")
                                        sortExpanded = false
                                    }
                                )
                            }
                        }
                        var showCatalogMenu by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { showCatalogMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More options")
                            }
                            DropdownMenu(
                                expanded = showCatalogMenu,
                                onDismissRequest = { showCatalogMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Generate new catalog") },
                                    onClick = {
                                        showPdfNameDialog = true
                                        showCatalogMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("View all catalogs") },
                                    onClick = {
                                        navController.navigate(Screen.Catalogs.route)
                                        showCatalogMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Apply Discount") },
                                    onClick = {
                                        productsViewModel.showDiscountDialog = true
                                        showCatalogMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        )
    }

    if (showSearchSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSearchSheet = false },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { showSearchSheet = false }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "Search",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                SearchContent(
                    viewModel = searchViewModel,
                    navController = navController,
                    onProductClick = {
                        showSearchSheet = false
                    }
                )
            }
        }
    }

    if (showPdfNameDialog) {
        var catalogName by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showPdfNameDialog = false }) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Enter catalog name")
                    TextField(
                        value = catalogName,
                        onValueChange = { catalogName = it },
                        label = { Text("Catalog Name") }
                    )
                    Button(onClick = {
                        productsViewModel.generatePdfCatalog(catalogName) {
                            navController.navigate(Screen.Catalogs.route)
                        }
                        showPdfNameDialog = false
                    }) {
                        Text("Generate")
                    }
                }
            }
        }
    }

    if (productsViewModel.showDiscountDialog) {
        var discount by remember { mutableStateOf("") }
        ModalBottomSheet(
            onDismissRequest = { productsViewModel.showDiscountDialog = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Enter discount percentage")
                TextField(
                    value = discount,
                    onValueChange = { discount = it },
                    label = { Text("Discount %") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        val discountValue = discount.toIntOrNull()
                        if (discountValue != null) {
                            productsViewModel.applyDiscount(discountValue)
                        }
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                productsViewModel.showDiscountDialog = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply")
                }
            }
        }
    }

    if (productsViewModel.applyingDiscount) {
        Dialog(onDismissRequest = {}) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Applying discount...")
                    CircularProgressIndicator()
                }
            }
        }
    }

    LaunchedEffect(discountAppliedMessage) {
        discountAppliedMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            lazyPagingItems.refresh()
            productsViewModel.onDiscountMessageShown()
        }
    }

    if (productsViewModel.showBoxNumberDialog) {
        var boxNumber by remember { mutableStateOf(filterParams.boxNumber ?: "") }
        val boxNumberSheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { productsViewModel.showBoxNumberDialog = false },
            sheetState = boxNumberSheetState,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Enter Box Number")
                TextField(
                    value = boxNumber,
                    onValueChange = { boxNumber = it },
                    label = { Text("Box Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        productsViewModel.updateBoxNumber(null)
                        scope.launch { boxNumberSheetState.hide() }.invokeOnCompletion {
                            if (!boxNumberSheetState.isVisible) {
                                productsViewModel.showBoxNumberDialog = false
                            }
                        }
                    }) {
                        Text("Clear")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        productsViewModel.updateBoxNumber(boxNumber.ifBlank { null })
                        scope.launch { boxNumberSheetState.hide() }.invokeOnCompletion {
                            if (!boxNumberSheetState.isVisible) {
                                productsViewModel.showBoxNumberDialog = false
                            }
                        }
                    }) {
                        Text("Apply")
                    }
                }
            }
        }
    }


    if (productsViewModel.generatingPdf) {
        Dialog(onDismissRequest = {}) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Generating PDF catalog...")
                    CircularProgressIndicator()
                }
            }
        }
    }

    DisposableEffect(pdfCatalogUrl) {
        val receiver = if (pdfCatalogUrl != null) {
            object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    val downloadManager =
                        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val query = DownloadManager.Query().setFilterById(id)
                    val cursor = downloadManager.query(query)
                    if (cursor.moveToFirst()) {
                        val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        if (columnIndex >= 0 && cursor.getInt(columnIndex) == DownloadManager.STATUS_SUCCESSFUL) {
                            val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                            if (uriIndex >= 0) {
                                val uriString = cursor.getString(uriIndex)
                                val downloadedFileUri = uriString.toUri()
                                val file = File(downloadedFileUri.path!!)
                                val fileUri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    file
                                )
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, fileUri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(
                                    Intent.createChooser(
                                        shareIntent,
                                        "Share Catalog"
                                    )
                                )
                                productsViewModel.onPdfShared()
                            }
                        }
                    }
                    cursor.close()
                }
            }
        } else {
            null
        }

        if (receiver != null) {
            val url = pdfCatalogUrl ?: ""
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fileName = "Product Catalog-${dateFormat.format(Date())}.pdf"

            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(url.toUri())
                .setTitle("Product Catalog")
                .setDescription("Downloading")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            downloadManager.enqueue(request)
            ContextCompat.registerReceiver(
                context,
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }

        onDispose {
            receiver?.let { context.unregisterReceiver(it) }
        }
    }


    if (productsViewModel.showPriceFilterSheet) {
        PriceFilterBottomSheet(
            viewModel = productsViewModel,
            currentPriceRange = filterParams.priceRange
        )
    }

    if (productsViewModel.showDiscountFilterSheet) {
        DiscountFilterBottomSheet(
            viewModel = productsViewModel,
            currentDiscountRange = filterParams.discountRange
        )
    }

    if (shareViewModel.shareState is ShareViewModel.ShareState.Processing) {
        Dialog(onDismissRequest = {}) {
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Preparing images...")
                    CircularProgressIndicator()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
        ) {

            var categoryExpanded by remember { mutableStateOf(false) }
            var tagsExpanded by remember { mutableStateOf(false) }
            var inStockExpanded by remember { mutableStateOf(false) }
            var flaggedExpanded by remember { mutableStateOf(false) }

            Box {
                FilterChip(
                    label = filterParams.selectedCategory,
                    onClick = { categoryExpanded = true })

                DropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false })
                {
                    DropdownMenuItem(text = { Text("All products") }, onClick = {
                        productsViewModel.updateSelectedCategory("All products")
                        categoryExpanded = false
                    })
                    categories.forEach { category ->
                        DropdownMenuItem(text = { Text(category.name) }, onClick = {
                            productsViewModel.updateSelectedCategory(category.name)
                            categoryExpanded = false
                        })
                    }
                }
            }

            Box {
                FilterChip(
                    label = if (filterParams.selectedTags.isEmpty()) "All Tags" else "${filterParams.selectedTags.size} tags",
                    onClick = { tagsExpanded = true }
                )
                DropdownMenu(
                    expanded = tagsExpanded,
                    onDismissRequest = { tagsExpanded = false }
                ) {
                    tags.forEach { tag ->
                        DropdownMenuItem(
                            text = { Text(tag) },
                            onClick = {
                                val currentTags = filterParams.selectedTags
                                val newTags = if (currentTags.contains(tag)) {
                                    currentTags - tag
                                } else {
                                    currentTags + tag
                                }
                                productsViewModel.updateSelectedTags(newTags)
                            },
                            leadingIcon = {
                                if (filterParams.selectedTags.contains(tag)) {
                                    Icon(Icons.Default.Check, contentDescription = "Selected")
                                 }
                            }
                        )
                    }
                }
            }

            FilterChip(
                label = filterParams.boxNumber ?: "Box Number",
                onClick = { productsViewModel.showBoxNumberDialog = true }
            )

            FilterChip(
                label = "₹${filterParams.priceRange.start.toInt()}-₹${filterParams.priceRange.endInclusive.toInt()}",
                onClick = { productsViewModel.showPriceFilterSheet = true })

            FilterChip(
                label = "${filterParams.discountRange.start.toInt()}%-${filterParams.discountRange.endInclusive.toInt()}%",
                onClick = { productsViewModel.showDiscountFilterSheet = true }
            )

            Box {
                FilterChip(
                    label = if (filterParams.excludeOutOfStock) "In Stock" else "All Stock",
                    onClick = { inStockExpanded = true }
                )
                DropdownMenu(
                    expanded = inStockExpanded,
                    onDismissRequest = { inStockExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All Stock") },
                        onClick = {
                            productsViewModel.updateExcludeOutOfStock(false)
                            inStockExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("In Stock") },
                        onClick = {
                            productsViewModel.updateExcludeOutOfStock(true)
                            inStockExpanded = false
                        }
                    )
                }
            }
            Box {
                FilterChip(
                    label = when (filterParams.flagged) {
                        true -> "Flagged"
                        false -> "Not Flagged"
                        else -> "All"
                    },
                    onClick = { flaggedExpanded = true }
                )
                DropdownMenu(
                    expanded = flaggedExpanded,
                    onDismissRequest = { flaggedExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All") },
                        onClick = {
                            productsViewModel.updateFlagged(null)
                            flaggedExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Flagged") },
                        onClick = {
                            productsViewModel.updateFlagged(true)
                            flaggedExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Not Flagged") },
                        onClick = {
                            productsViewModel.updateFlagged(false)
                            flaggedExpanded = false
                        }
                    )
                }
            }
        }
        val refreshing = lazyPagingItems.loadState.refresh is LoadState.Loading
        val pullRefreshState = rememberPullRefreshState(refreshing, { lazyPagingItems.refresh() })
        Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (lazyPagingItems.loadState.refresh == LoadState.Loading) {
                    items(10) {
                        ShimmerEffect(modifier = Modifier.aspectRatio(0.7f))
                    }
                }

                items(lazyPagingItems.itemCount) { index ->
                    lazyPagingItems[index]?.let { product ->
                        ProductCard(
                            product = product,
                            isSelected = productsViewModel.selectedProducts.contains(product),
                            onClick = {
                                if (productsViewModel.selectionMode) {
                                    productsViewModel.onProductClicked(product)
                                } else {
                                    navController.navigate(Screen.ProductDetail.route + "/${product.id}")
                                }
                            },
                            onLongClick = { productsViewModel.onProductLongClicked(product) }
                        )
                    }
                }

                if (lazyPagingItems.loadState.append == LoadState.Loading) {
                    item {
                        ShimmerEffect(modifier = Modifier.aspectRatio(0.7f))
                    }
                }
            }
            PullRefreshIndicator(refreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContent(
    viewModel: SearchViewModel,
    navController: NavController,
    onProductClick: () -> Unit
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val skuNumber by viewModel.skuNumber.collectAsState()
    val selectedPrefix by viewModel.selectedPrefix.collectAsState()
    val prefixes = viewModel.skuPrefixes

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                    modifier = Modifier.fillMaxWidth(),
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
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
                    ProductItem(product = product, onClick = {
                        navController.navigate(Screen.ProductDetail.route + "/${product.id}")
                        onProductClick()
                    })
                }
            }
        }
    }
}

@Composable
fun ProductItem(product: ApiProduct, onClick: () -> Unit) {
    var showZoomedImage by rememberSaveable { mutableStateOf(false) }

    if (showZoomedImage) {
        ZoomableImage(
            imageUrl = product.images.first().imageUrl,
            onDismiss = { showZoomedImage = false }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            .padding(8.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        SubcomposeAsyncImage(
            model = product.images.first().imageUrl,
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
            Text(text = "MRP: ₹${product.mrp}")
            Text(text = "Selling Price: ₹${product.price}")
            val outOfStock = !product.inStock
            if (outOfStock) {
                Text(
                    text = "Out of stock",
                    color = Color.Red,
                )
            }
            Text(text = "Product Code: ${product.priceCode}")
        }
    }
}

@Composable
private fun ZoomableImage(imageUrl: String, onDismiss: () -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onDismiss() }
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


@Composable
fun FilterChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(32.dp) // Reduced height
            .clip(RoundedCornerShape(8.dp))
            .background(Color.LightGray.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, fontSize = 11.sp)
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceFilterBottomSheet(
    viewModel: ProductsViewModel,
    currentPriceRange: ClosedFloatingPointRange<Float>
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = { viewModel.showPriceFilterSheet = false },
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Price Range",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "₹${currentPriceRange.start.toInt()}",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start
                )
                Text(
                    text = "₹${currentPriceRange.endInclusive.toInt()}",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.End
                )
            }

            RangeSlider(
                value = currentPriceRange,
                onValueChange = { viewModel.updatePriceRange(it) },
                valueRange = 0f..5000f,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    viewModel.updatePriceRange(0f..5000f)
                }) {
                    Text("Reset")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            viewModel.showPriceFilterSheet = false
                        }
                    }
                }) {
                    Text("Done")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscountFilterBottomSheet(
    viewModel: ProductsViewModel,
    currentDiscountRange: ClosedFloatingPointRange<Float>
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = { viewModel.showDiscountFilterSheet = false },
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Discount Range",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${currentDiscountRange.start.toInt()}%",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Start
                )
                Text(
                    text = "${currentDiscountRange.endInclusive.toInt()}%",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.End
                )
            }

            RangeSlider(
                value = currentDiscountRange,
                onValueChange = { viewModel.updateDiscountRange(it) },
                valueRange = 0f..100f,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    viewModel.updateDiscountRange(0f..100f)
                }) {
                    Text("Reset")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            viewModel.showDiscountFilterSheet = false
                        }
                    }
                }) {
                    Text("Done")
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductCard(product: ApiProduct, isSelected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            SubcomposeAsyncImage(
                model = product.images.first().imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (product.flagged) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = "Flagged",
                    tint = Color.Red,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                )
            }
            if (product.boxNumber != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                            ),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Box: ${product.boxNumber}",
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                            startY = 0f,
                            endY = 400f,
                        )
                    )
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.sku,
                    color = Color.White
                )
                Text(
                    text = "₹${product.price}",
                    color = Color.White
                )
                val outOfStock = !product.inStock
                if (outOfStock) {
                    Text(
                        text = "Out of stock",
                        color = Color.Red
                    )
                }
            }
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color.Transparent, shape = CircleShape)
                    )
                }
            }
        }
    }
}
