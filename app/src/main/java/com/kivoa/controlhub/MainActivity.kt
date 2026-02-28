package com.kivoa.controlhub

import android.app.Application
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.kivoa.controlhub.api.RetrofitInstance
import com.kivoa.controlhub.data.Prompt
import com.kivoa.controlhub.ui.screens.AmazonProductListScreen
import com.kivoa.controlhub.ui.screens.ProductsViewModel
import com.kivoa.controlhub.ui.screens.CatalogsScreen
import com.kivoa.controlhub.ui.screens.CreateScreen
import com.kivoa.controlhub.ui.screens.EditProductScreen
import com.kivoa.controlhub.ui.screens.HomeScreen
import com.kivoa.controlhub.ui.screens.OrderDetailScreen
import com.kivoa.controlhub.ui.screens.OrdersScreen
import com.kivoa.controlhub.ui.screens.OrdersViewModel
import com.kivoa.controlhub.ui.screens.OrdersViewModelFactory
import com.kivoa.controlhub.ui.screens.ProductDetailScreen
import com.kivoa.controlhub.ui.screens.ProductsScreen
import com.kivoa.controlhub.ui.screens.ReorderImagesScreen
import com.kivoa.controlhub.ui.screens.ShareViewModel
import com.kivoa.controlhub.ui.screens.ShareViewModelFactory
import com.kivoa.controlhub.ui.screens.settings.CategoriesScreen
import com.kivoa.controlhub.ui.screens.settings.CategoryDetailScreen
import com.kivoa.controlhub.ui.screens.settings.CategoryPromptsScreen
import com.kivoa.controlhub.ui.screens.settings.CategoryPromptsViewModel
import com.kivoa.controlhub.ui.screens.settings.CategoryPromptsViewModelFactory
import com.kivoa.controlhub.ui.screens.settings.CreateCategoryScreen
import com.kivoa.controlhub.ui.screens.settings.CreatePromptScreen
import com.kivoa.controlhub.ui.screens.settings.EditCategoryScreen
import com.kivoa.controlhub.ui.screens.settings.EditPromptScreen
import com.kivoa.controlhub.ui.screens.settings.SettingsScreen
import com.kivoa.controlhub.ui.screens.settings.SettingsViewModel
import com.kivoa.controlhub.ui.theme.ControlHubTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class AppBarState(
    val title: @Composable () -> Unit = { },
    val navigationIcon: @Composable () -> Unit = { },
    val actions: @Composable RowScope.() -> Unit = { }
)

class AppBarViewModel : ViewModel() {
    private val _appBarState = MutableStateFlow(AppBarState())
    val appBarState: StateFlow<AppBarState> = _appBarState

    fun setAppBarState(state: AppBarState) {
        _appBarState.value = state
    }
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val theme by settingsViewModel.theme.collectAsState()

            ControlHubTheme(
                darkTheme = when (theme) {
                    "Light" -> false
                    "Dark" -> true
                    else -> isSystemInDarkTheme()
                }
            ) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val context = LocalContext.current
                val application = context.applicationContext as Application
                val apiService = RetrofitInstance.api
                val shareViewModelFactoryForDetail =
                    remember { ShareViewModelFactory(application) }

                val productsViewModel: ProductsViewModel = viewModel()
                val appBarViewModel: AppBarViewModel = viewModel()
                val appBarState by appBarViewModel.appBarState.collectAsState()


                Scaffold(
                    topBar = { KivoaAppBar(appBarState = appBarState) },
                    bottomBar = {
                        NavigationBar {
                            val items = listOf(
                                Screen.Products,
                                Screen.Create,
                                Screen.Amazon,
                                Screen.Orders,
                                Screen.Settings,
                            )
                            items.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon!!, contentDescription = null) },
                                    label = { Text(screen.route) },
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
                        startDestination = Screen.Products.route,
                        Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Search.route) {
                            HomeScreen(
                                modifier = Modifier.fillMaxSize(),
                                navController = navController,
                                appBarViewModel = appBarViewModel
                            )
                        }
                        composable(Screen.Products.route) {
                            ProductsScreen(
                                navController = navController,
                                productsViewModel = productsViewModel,
                                appBarViewModel = appBarViewModel
                            )
                        }
                        composable(Screen.Amazon.route) {
                            AmazonProductListScreen(
                                navController = navController, 
                                appBarViewModel = appBarViewModel
                            )
                        }
                        composable(
                            route = Screen.Create.route + "?tabIndex={tabIndex}",
                            arguments = listOf(navArgument("tabIndex") {
                                type = NavType.IntType
                                defaultValue = 0
                            })
                        ) { backStackEntry ->
                            val tabIndex = backStackEntry.arguments?.getInt("tabIndex") ?: 0
                            CreateScreen(
                                appBarViewModel = appBarViewModel,
                                navController = navController,
                                initialTabIndex = tabIndex
                            )
                        }
                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                navController = navController,
                                appBarViewModel = appBarViewModel,
                                settingsViewModel = settingsViewModel
                            )
                        }
                        composable(Screen.SettingsCategories.route) {
                            CategoriesScreen(
                                navController = navController,
                                appBarViewModel = appBarViewModel
                            )
                        }
                        composable(Screen.CreateCategory.route) {
                            CreateCategoryScreen(
                                navController = navController,
                                appBarViewModel = appBarViewModel
                            ) { navController.popBackStack() }
                        }
                        composable(Screen.Catalogs.route) {
                            CatalogsScreen(navController = navController, appBarViewModel = appBarViewModel)
                        }
                        composable(
                            route = Screen.ProductDetail.route,
                            arguments = listOf(navArgument("productId") { type = NavType.LongType })
                        ) {
                            val productId = it.arguments?.getLong("productId")
                            val shareViewModelForDetail: ShareViewModel =
                                viewModel(factory = shareViewModelFactoryForDetail)
                            ProductDetailScreen(
                                productId = productId!!,
                                navController = navController,
                                shareViewModel = shareViewModelForDetail,
                                appBarViewModel = appBarViewModel
                            )
                        }
                        composable(
                            route = Screen.EditProduct.route + "/{productId}?tabIndex={tabIndex}",
                            arguments = listOf(
                                navArgument("productId") { type = NavType.LongType },
                                navArgument("tabIndex") {
                                    type = NavType.IntType
                                    defaultValue = 0
                                }
                            )
                        ) { backStackEntry ->
                            val productId = backStackEntry.arguments?.getLong("productId")
                            EditProductScreen(
                                productId = productId!!,
                                navController = navController,
                                appBarViewModel = appBarViewModel
                            )
                        }
                        composable(
                            route = Screen.ReorderImages.route + "/{productId}",
                            arguments = listOf(navArgument("productId") { type = NavType.LongType })
                        ) {
                            val productId = it.arguments?.getLong("productId")
                            ReorderImagesScreen(
                                productId = productId!!,
                                navController = navController,
                                appBarViewModel = appBarViewModel
                            )
                        }
                        composable(
                            route = Screen.CategoryDetail.route + "/{categoryJson}",
                            arguments = listOf(navArgument("categoryJson") { type = NavType.StringType })
                        ) {
                            val categoryJson = it.arguments?.getString("categoryJson")
                            CategoryDetailScreen(
                                navController = navController,
                                appBarViewModel = appBarViewModel,
                                categoryJson = categoryJson
                            )
                        }
                        composable(
                            route = Screen.EditCategory.route + "/{categoryJson}",
                            arguments = listOf(navArgument("categoryJson") { type = NavType.StringType })
                        ) {
                            val categoryJson = it.arguments?.getString("categoryJson")
                            val category = Gson().fromJson(
                                categoryJson,
                                com.kivoa.controlhub.data.ApiCategory::class.java
                            )
                            if (category != null) {
                                EditCategoryScreen(
                                    navController = navController,
                                    appBarViewModel = appBarViewModel,
                                    category = category
                                ) { navController.popBackStack() }
                            }
                        }
                        composable(
                            route = Screen.CategoryPrompts.route,
                            arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
                        ) {
                            val categoryName = it.arguments?.getString("categoryName")
                            val viewModel: CategoryPromptsViewModel =
                                viewModel(factory = CategoryPromptsViewModelFactory(apiService))
                            CategoryPromptsScreen(
                                navController = navController,
                                categoryName = categoryName!!,
                                viewModel = viewModel,
                                appBarViewModel = appBarViewModel
                            )
                        }
                        composable(
                            route = Screen.EditPrompt.route,
                            arguments = listOf(navArgument("promptJson") { type = NavType.StringType })
                        ) {
                            val promptJson = it.arguments?.getString("promptJson")
                            val prompt = Gson().fromJson(promptJson, Prompt::class.java)
                            EditPromptScreen(
                                navController = navController,
                                prompt = prompt,
                                appBarViewModel = appBarViewModel
                            )
                        }
                        composable(
                            route = Screen.CreatePrompt.route,
                            arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
                        ) {
                            val categoryName = it.arguments?.getString("categoryName")
                            CreatePromptScreen(
                                navController = navController,
                                categoryName = categoryName!!,
                                appBarViewModel = appBarViewModel
                            )
                        }
                        composable(Screen.Orders.route) {
                            val ordersViewModel: OrdersViewModel =
                                viewModel(factory = OrdersViewModelFactory(apiService))
                            OrdersScreen(
                                navController = navController,
                                appBarViewModel = appBarViewModel,
                                ordersViewModel = ordersViewModel
                            )
                        }
                        composable(
                            route = Screen.OrderDetail.route,
                            arguments = listOf(navArgument("orderJson") { type = NavType.StringType })
                        ) {
                            val orderJson = it.arguments?.getString("orderJson")
                            val order = Gson().fromJson(
                                orderJson,
                                com.kivoa.controlhub.data.shopify.order.Order::class.java
                            )
                            if (order != null) {
                                OrderDetailScreen(
                                    order = order,
                                    navController = navController,
                                    appBarViewModel = appBarViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KivoaAppBar(appBarState: AppBarState) {
    TopAppBar(
        title = appBarState.title,
        navigationIcon = appBarState.navigationIcon,
        actions = appBarState.actions
    )
}

sealed class Screen(val route: String, val icon: ImageVector? = null) {
    object Search : Screen("Search", Icons.Default.Search)
    object Products : Screen("Products", Icons.Default.ShoppingCart)
    object Amazon : Screen("Amazon", Icons.Default.Store)
    object Create : Screen("Create", Icons.Default.AddCircle)
    object ProductDetail : Screen("product/{productId}")
    object EditProduct : Screen("edit_product")
    object ReorderImages : Screen("reorder_images")
    object Settings : Screen("Settings", Icons.Default.Settings)
    object SettingsCategories : Screen("Settings/Categories")
    object CreateCategory : Screen("CreateCategory")
    object CategoryDetail : Screen("CategoryDetail")
    object EditCategory : Screen("EditCategory")
    object CategoryPrompts : Screen("Settings/Categories/{categoryName}/Prompts")
    object EditPrompt : Screen("Settings/Prompts/{promptJson}")
    object CreatePrompt : Screen("Settings/Categories/{categoryName}/CreatePrompt")
    object Catalogs : Screen("Catalogs")
    object Orders : Screen("Orders", Icons.Default.ShoppingBasket)
    object OrderDetail : Screen("OrderDetail/{orderJson}")

    fun withArgs(vararg args: Any): String {
        var finalRoute = route
        args.forEach { arg ->
            finalRoute = finalRoute.replaceFirst(
                Regex("\\{[^}]+\\}"),
                Uri.encode(arg.toString())
            )
        }
        return finalRoute
    }
}