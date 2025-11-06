package com.kivoa.controlhub

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Settings // Added import for Settings icon
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
import androidx.compose.ui.unit.dp
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
import com.kivoa.controlhub.ui.screens.BrowseScreen
import com.kivoa.controlhub.ui.screens.BrowseViewModel
import com.kivoa.controlhub.ui.screens.CreateScreen
import com.kivoa.controlhub.ui.screens.EditProductScreen
import com.kivoa.controlhub.ui.screens.HomeScreen
import com.kivoa.controlhub.ui.screens.ProductDetailScreen
import com.kivoa.controlhub.ui.screens.ShareViewModel
import com.kivoa.controlhub.ui.screens.ShareViewModelFactory
import com.kivoa.controlhub.ui.screens.settings.CategoriesScreen // Added CategoriesScreen import
import com.kivoa.controlhub.ui.screens.settings.CategoryDetailScreen
import com.kivoa.controlhub.ui.screens.settings.CreateCategoryScreen // Added CreateCategoryScreen import
import com.kivoa.controlhub.ui.screens.settings.EditCategoryScreen // Added EditCategoryScreen import
import com.kivoa.controlhub.ui.screens.settings.SettingsScreen // Added SettingsScreen import
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
            ControlHubTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val currentScreen = Screen.fromRoute(currentDestination?.route)

                val context = LocalContext.current
                val application = context.applicationContext as Application
                val apiService = RetrofitInstance.api
                // For ProductDetailScreen, no refresh is needed, so pass null
                val shareViewModelFactoryForDetail =
                    remember { ShareViewModelFactory(application, apiService, null) }

                val browseViewModel: BrowseViewModel = viewModel()
                val appBarViewModel: AppBarViewModel = viewModel()
                val appBarState by appBarViewModel.appBarState.collectAsState()


                Scaffold(
                    topBar = { KivoaAppBar(appBarState = appBarState) },
                    bottomBar = {
                        NavigationBar(modifier = Modifier.height(100.dp)) {
                            val items = listOf(
                                Screen.Search,
                                Screen.Browse,
                                Screen.Create,
                                Screen.Settings, // Added Settings to the bottom navigation bar
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
                        composable(Screen.Search.route) { HomeScreen(modifier = Modifier.fillMaxSize(), navController = navController, appBarViewModel = appBarViewModel) }
                        composable(Screen.Browse.route) { BrowseScreen(navController = navController, browseViewModel = browseViewModel, appBarViewModel = appBarViewModel) }
                        composable(Screen.Create.route) { CreateScreen(appBarViewModel = appBarViewModel) }
                        composable(Screen.Settings.route) { SettingsScreen(navController = navController, appBarViewModel = appBarViewModel) }
                        composable(Screen.SettingsCategories.route) { CategoriesScreen(navController = navController, appBarViewModel = appBarViewModel) }
                        composable(Screen.CreateCategory.route) {
                            CreateCategoryScreen(navController = navController, appBarViewModel = appBarViewModel) { navController.popBackStack() } // Navigate back on success
                        }
                        composable(
                            route = Screen.ProductDetail.route + "/{productId}",
                            arguments = listOf(navArgument("productId") { type = NavType.LongType })
                        ) {
                            val productId = it.arguments?.getLong("productId")
                            val shareViewModelForDetail: ShareViewModel = viewModel(factory = shareViewModelFactoryForDetail)
                            ProductDetailScreen(productId = productId!!, navController = navController, shareViewModel = shareViewModelForDetail, appBarViewModel = appBarViewModel)
                        }
                        composable(
                            route = Screen.EditProduct.route + "/{productId}",
                            arguments = listOf(navArgument("productId") { type = NavType.LongType })
                        ) {
                            val productId = it.arguments?.getLong("productId")
                            EditProductScreen(productId = productId!!, navController = navController)
                        }
                        composable(
                            route = Screen.CategoryDetail.route + "/{categoryJson}",
                            arguments = listOf(navArgument("categoryJson") { type = NavType.StringType })
                        ) {
                            val categoryJson = it.arguments?.getString("categoryJson")
                            CategoryDetailScreen(navController = navController, appBarViewModel = appBarViewModel, categoryJson = categoryJson)
                        }
                        composable(
                            route = Screen.EditCategory.route + "/{categoryJson}",
                            arguments = listOf(navArgument("categoryJson") { type = NavType.StringType })
                        ) {
                            val categoryJson = it.arguments?.getString("categoryJson")
                            val category = Gson().fromJson(categoryJson, com.kivoa.controlhub.data.ApiCategory::class.java)
                            if (category != null) {
                                EditCategoryScreen(navController = navController, appBarViewModel = appBarViewModel, category = category) { navController.popBackStack() } // Navigate back on success
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
    object Browse : Screen("Browse", Icons.Default.ShoppingCart)
    object Create : Screen("Create", Icons.Default.AddCircle)
    object ProductDetail : Screen("ProductDetail")
    object EditProduct : Screen("edit_product")
    object Settings : Screen("Settings", Icons.Default.Settings) // Added Settings object
    object SettingsCategories : Screen("Settings/Categories")
    object CreateCategory : Screen("CreateCategory")
    object CategoryDetail : Screen("CategoryDetail")
    object EditCategory : Screen("EditCategory") // Added EditCategory object


    companion object {
        fun fromRoute(route: String?): Screen {
            return when (route?.substringBefore("/")) {
                Search.route -> Search
                Browse.route -> Browse
                Create.route -> Create
                ProductDetail.route -> ProductDetail
                EditProduct.route -> EditProduct
                Settings.route -> Settings
                SettingsCategories.route.substringBefore("/") -> SettingsCategories
                CreateCategory.route -> CreateCategory
                CategoryDetail.route -> CategoryDetail
                EditCategory.route -> EditCategory // Added EditCategory route
                else -> Search
            }
        }
    }
}