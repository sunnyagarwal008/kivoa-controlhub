package com.kivoa.controlhub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.kivoa.controlhub.data.Product
import com.kivoa.controlhub.ui.screens.BrowseScreen
import com.kivoa.controlhub.ui.screens.BrowseViewModel
import com.kivoa.controlhub.ui.screens.CreateScreen
import com.kivoa.controlhub.ui.screens.HomeScreen
import com.kivoa.controlhub.ui.screens.ProductDetailScreen
import com.kivoa.controlhub.ui.screens.ShareViewModel
import com.kivoa.controlhub.ui.theme.ControlHubTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ControlHubTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val currentScreen = Screen.fromRoute(currentDestination?.route)

                val browseViewModel: BrowseViewModel = viewModel()
                val shareViewModel: ShareViewModel = viewModel()
                var product by remember { mutableStateOf<Product?>(null) }

                Scaffold(
                    topBar = { KivoaAppBar(screen = currentScreen, navController = navController, browseViewModel = browseViewModel, shareViewModel = shareViewModel, product = product) },
                    bottomBar = {
                        NavigationBar(modifier = Modifier.height(100.dp)) {
                            val items = listOf(
                                Screen.Search,
                                Screen.Browse,
                                Screen.Create,
                            )
                            items.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon!!, contentDescription = null) },
                                    label = { Text(screen.route, modifier = Modifier.offset(y = (-4).dp)) },
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController,
                        startDestination = Screen.Search.route,
                        Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Search.route) { HomeScreen(modifier = Modifier.fillMaxSize(), navController = navController) }
                        composable(Screen.Browse.route) { BrowseScreen(navController = navController, browseViewModel = browseViewModel, shareViewModel = shareViewModel) }
                        composable(Screen.Create.route) { CreateScreen() }
                        composable(
                            route = Screen.ProductDetail.route + "/{productJson}",
                            arguments = listOf(navArgument("productJson") { type = NavType.StringType })
                        ) {
                            val productJson = it.arguments?.getString("productJson")
                            product = Gson().fromJson(productJson, Product::class.java)
                            ProductDetailScreen(product = product!!)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KivoaAppBar(screen: Screen, navController: NavController, browseViewModel: BrowseViewModel, shareViewModel: ShareViewModel, product: Product?) {
    TopAppBar(
        title = {
            if (browseViewModel.selectionMode) {
                Text("${browseViewModel.selectedProducts.size} selected")
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.kivoa_logo),
                        contentDescription = "Kivoa Logo",
                        modifier = Modifier.size(32.dp)
                    )
                    Text(screen.route, modifier = Modifier.padding(start = 8.dp))
                }
            }
        },
        navigationIcon = {
            if (browseViewModel.selectionMode) {
                IconButton(onClick = {
                    browseViewModel.selectionMode = false
                    browseViewModel.selectedProducts = emptySet()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            } else if (screen == Screen.ProductDetail) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            if (browseViewModel.selectionMode) {
                IconButton(onClick = { shareViewModel.shareProducts(browseViewModel.selectedProducts) }) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
            } else if (screen == Screen.ProductDetail) {
                IconButton(onClick = { product?.let { shareViewModel.shareProduct(it) } }) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
            }
        }
    )
}

sealed class Screen(val route: String, val icon: ImageVector? = null) {
    object Search : Screen("Search", Icons.Default.Search)
    object Browse : Screen("Browse", Icons.Default.ShoppingCart)
    object Create : Screen("Create", Icons.Default.AddCircle)
    object ProductDetail : Screen("ProductDetail")

    companion object {
        fun fromRoute(route: String?): Screen {
            return when (route?.substringBefore("/")) {
                Search.route -> Search
                Browse.route -> Browse
                Create.route -> Create
                ProductDetail.route -> ProductDetail
                else -> Search
            }
        }
    }
}
