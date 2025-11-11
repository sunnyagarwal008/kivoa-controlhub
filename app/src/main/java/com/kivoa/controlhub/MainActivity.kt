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
import com.kivoa.controlhub.data.Prompt
import com.kivoa.controlhub.ui.screens.CatalogsScreen
import com.kivoa.controlhub.ui.screens.BrowseScreen
import com.kivoa.controlhub.ui.screens.BrowseViewModel
import com.kivoa.controlhub.ui.screens.CreateScreen
import com.kivoa.controlhub.ui.screens.EditProductScreen
import com.kivoa.controlhub.ui.screens.HomeScreen
import com.kivoa.controlhub.ui.screens.ProductDetailScreen
import com.kivoa.controlhub.ui.screens.ReorderImagesScreen
import com.kivoa.controlhub.ui.screens.ShareViewModel
import com.kivoa.controlhub.ui.screens.ShareViewModelFactory
import com.kivoa.controlhub.ui.screens.settings.CategoriesScreen // Added CategoriesScreen import
import com.kivoa.controlhub.ui.screens.settings.CategoryDetailScreen
import com.kivoa.controlhub.ui.screens.settings.CategoryPromptsScreen
import com.kivoa.controlhub.ui.screens.settings.CategoryPromptsViewModel
import com.kivoa.controlhub.ui.screens.settings.CategoryPromptsViewModelFactory
import com.kivoa.controlhub.ui.screens.settings.CreateCategoryScreen // Added CreateCategoryScreen import
import com.kivoa.controlhub.ui.screens.settings.CreatePromptScreen
import com.kivoa.controlhub.ui.screens.settings.EditCategoryScreen // Added EditCategoryScreen import
import com.kivoa.controlhub.ui.screens.settings.EditPromptScreen
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
                        composable(Screen.Settings.route) { SettingsScreen(navController = navController, appBarViewModel = appBarViewModel) }
                        composable(Screen.SettingsCategories.route) { CategoriesScreen(navController = navController, appBarViewModel = appBarViewModel) }
                        composable(Screen.CreateCategory.route) {
                            CreateCategoryScreen(navController = navController, appBarViewModel = appBarViewModel) { navController.popBackStack() } // Navigate back on success
                        }
                        composable(Screen.Catalogs.route) {
                            CatalogsScreen(navController = navController, appBarViewModel = appBarViewModel)
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
                            EditProductScreen(productId = productId!!, navController = navController)
                        }
                        composable(
                            route = Screen.ReorderImages.route + "/{productId}",
                            arguments = listOf(navArgument("productId") { type = NavType.LongType })
                        ) {
                            val productId = it.arguments?.getLong("productId")
                            ReorderImagesScreen(productId = productId!!, navController = navController, appBarViewModel = appBarViewModel)
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
                        composable(
                            route = Screen.CategoryPrompts.route,
                            arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
                        ) {
                            val categoryName = it.arguments?.getString("categoryName")
                            val viewModel: CategoryPromptsViewModel = viewModel(factory = CategoryPromptsViewModelFactory(apiService))
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
    object ReorderImages : Screen("reorder_images")
    object Settings : Screen("Settings", Icons.Default.Settings) // Added Settings object
    object SettingsCategories : Screen("Settings/Categories")
    object CreateCategory : Screen("CreateCategory")
    object CategoryDetail : Screen("CategoryDetail")
    object EditCategory : Screen("EditCategory") // Added EditCategory object
    object CategoryPrompts : Screen("Settings/Categories/{categoryName}/Prompts")
    object EditPrompt : Screen("Settings/Prompts/{promptJson}")
    object CreatePrompt : Screen("Settings/Categories/{categoryName}/CreatePrompt")
    object Catalogs : Screen("Catalogs")

    fun withArgs(vararg args: Any): String {
        var finalRoute = route
        args.forEach { arg ->
            finalRoute = finalRoute.replaceFirst(Regex("\\{[^}]+\\}"), arg.toString())
        }
        return finalRoute
    }
}
