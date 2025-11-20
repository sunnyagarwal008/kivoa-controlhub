package com.kivoa.controlhub.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.kivoa.controlhub.AppBarState
import com.kivoa.controlhub.AppBarViewModel
import com.kivoa.controlhub.api.RetrofitInstance
import com.kivoa.controlhub.data.UpdateProductRequest
import java.math.BigDecimal
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EditProductScreen(
    productId: Long,
    navController: NavController,
    appBarViewModel: AppBarViewModel,
    editProductViewModel: EditProductViewModel = viewModel(
        factory = EditProductViewModelFactory(RetrofitInstance.api)
    )
) {
    val product by editProductViewModel.product.collectAsState()
    val updateState by editProductViewModel.updateState.collectAsState()
    val categories by editProductViewModel.categories.collectAsState()
    var showRawImage by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        editProductViewModel.getProductById(productId)
    }

    LaunchedEffect(product) {
        product?.let {
            appBarViewModel.setAppBarState(
                AppBarState(
                    title = { Text("Edit Product") },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        if (it.rawImage.isNotBlank()) {
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

    if (showRawImage) {
        product?.rawImage?.let {
            RawImageDialog(
                imageUrl = it,
                onDismiss = { showRawImage = false }
            )
        }
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
        var title by remember { mutableStateOf(product!!.title ?: "") }
        var description by remember { mutableStateOf(product!!.description ?: "") }
        val categoryTags = remember(product!!.categoryDetails.tags) {
            product!!.categoryDetails.tags.split(",").map { it.trim() }
        }
        val initialProductTags = remember(product!!.tags) {
            product!!.tags?.split(",")?.map { it.trim() }?.toSet() ?: emptySet()
        }
        var selectedTags by remember {
            mutableStateOf(initialProductTags)
        }
        val extraTags = initialProductTags.filter { it !in categoryTags }
        val allDisplayTags = (extraTags + categoryTags).distinct()


        var boxNumber by remember { mutableStateOf(product!!.boxNumber?.toString() ?: "") }
        var expanded by remember { mutableStateOf(false) }
        var categoryExpanded by remember { mutableStateOf(false) }
        var additionalDetailsExpanded by remember { mutableStateOf(false) }

        var weight by remember { mutableStateOf(product!!.weight?.toString() ?: "") }
        var length by remember { mutableStateOf(product!!.dimensions?.length?.toString() ?: "") }
        var breadth by remember { mutableStateOf(product!!.dimensions?.breadth?.toString() ?: "") }
        var height by remember { mutableStateOf(product!!.dimensions?.height?.toString() ?: "") }
        var size by remember { mutableStateOf(product!!.size ?: "") }

        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (product!!.images.isNotEmpty()) {
                val pagerState = rememberPagerState(
                    initialPage = 0,
                    initialPageOffsetFraction = 0f
                ) {
                    product!!.images.size
                }
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth(),
                    ) { page ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.33f)
                        ) {
                            AsyncImage(
                                model = product!!.images[page].imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Row(
                        Modifier
                            .height(50.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(product!!.images.size) { iteration ->
                            val color =
                                if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                            Box(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .size(16.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption.name) },
                            onClick = {
                                category = selectionOption.name
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }
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
                    onValueChange = { newValue ->
                        if (newValue.matches(Regex("""^\d*\.?\d*$"""))) {
                            mrp = newValue
                            val mrpBigDecimal = newValue.toBigDecimalOrNull()
                            val discountBigDecimal = discount.toBigDecimalOrNull()
                            if (mrpBigDecimal != null && discountBigDecimal != null) {
                                val sellingPrice =
                                    mrpBigDecimal - (mrpBigDecimal * discountBigDecimal.divide(
                                        BigDecimal(100)
                                    ))
                                price = NumberFormat.getInstance().format(sellingPrice)
                            }
                        }
                    },
                    label = { Text("MRP") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextField(
                    value = discount,
                    onValueChange = { newValue ->
                        if (newValue.matches(Regex("""^\d*\.?\d*$"""))) {
                            discount = newValue
                            val mrpBigDecimal = mrp.toBigDecimalOrNull()
                            val discountBigDecimal = newValue.toBigDecimalOrNull()
                            if (mrpBigDecimal != null && discountBigDecimal != null) {
                                val sellingPrice =
                                    mrpBigDecimal - (mrpBigDecimal * discountBigDecimal.divide(
                                        BigDecimal(100)
                                    ))
                                price = NumberFormat.getInstance().format(sellingPrice)
                            }
                        }
                    },
                    label = { Text("Discount") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = price,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Price") },
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
                    allDisplayTags.forEach { tag ->
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

            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { additionalDetailsExpanded = !additionalDetailsExpanded }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Additional Details", style = MaterialTheme.typography.titleMedium)
                    Icon(
                        imageVector = if (additionalDetailsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand or collapse"
                    )
                }
                AnimatedVisibility(visible = additionalDetailsExpanded) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { newValue ->
                                weight = newValue
                            },
                            label = { Text("Weight (grams)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            OutlinedTextField(
                                value = length,
                                onValueChange = { newValue ->
                                    length = newValue
                                },
                                label = { Text("Length (mm)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = breadth,
                                onValueChange = { newValue ->
                                    breadth = newValue
                                },
                                label = { Text("Breadth (mm)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = height,
                                onValueChange = { newValue ->
                                    height = newValue
                                },
                                label = { Text("Height (mm)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = size,
                            onValueChange = { newValue ->
                                size = newValue
                            },
                            label = { Text("Size") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val request = UpdateProductRequest(
                        title = title,
                        description = description,
                        category = category,
                        purchaseMonth = purchaseMonth,
                        mrp = mrp.toDouble(),
                        price = NumberFormat.getInstance().parse(price).toDouble(),
                        discount = discount.toDouble(),
                        gst = gst.toDouble(),
                        priceCode = priceCode,
                        tags = selectedTags.joinToString(","),
                        boxNumber = boxNumber.toIntOrNull(),
                        weight = weight.toDoubleOrNull(),
                        length = length.toDoubleOrNull(),
                        breadth = breadth.toDoubleOrNull(),
                        height = height.toDoubleOrNull(),
                        size = size
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