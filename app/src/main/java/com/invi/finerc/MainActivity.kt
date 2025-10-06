package com.invi.finerc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.invi.finerc.ui.component.TransactionDetailScreen
import com.invi.finerc.ui.screen.AllTransactionsScreen
import com.invi.finerc.ui.screen.CollectionDetailScreen
import com.invi.finerc.ui.screen.CollectionEditScreen
import com.invi.finerc.ui.screen.CollectionsScreen
import com.invi.finerc.ui.screen.EditTransactionScreen
import com.invi.finerc.ui.screen.OverviewScreen
import com.invi.finerc.ui.screen.ReportScreen
import com.invi.finerc.ui.screen.SettingsScreen
import com.invi.finerc.ui.theme.FinercTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinercTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0A0A0A) // CRED dark background
                ) {
                    FinercApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinercApp() {
    val navController = rememberNavController()

    var selectedGroupId by rememberSaveable { mutableStateOf<Long?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1A1A1A),
                contentColor = Color.White
            ) {
                val items = listOf(
                    BottomNavItem("overview", "Overview", Icons.Default.Home),
                    BottomNavItem("reports", "Reports", Icons.Default.BarChart),
                    BottomNavItem("collections", "Collections", Icons.Default.Category),
                    BottomNavItem("settings", "Settings", Icons.Default.Settings)
                )
                val currentRoute =
                    navController.currentBackStackEntryAsState().value?.destination?.route

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title, fontSize = 12.sp) },
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00D4AA),
                            selectedTextColor = Color(0xFF00D4AA),
                            unselectedIconColor = Color(0xFF666666),
                            unselectedTextColor = Color(0xFF666666)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "overview",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("overview") { OverviewScreen(navController, ) }
            composable("reports") { ReportScreen() }

            composable("collections") {
                CollectionsScreen(navController = navController)
            }
            composable(
                route = "collectionDetail/{collectionId}",
                arguments = listOf(navArgument("collectionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val collectionId = backStackEntry.arguments?.getLong("collectionId") ?: 0L
                CollectionDetailScreen(collectionId = collectionId, navController = navController)
            }
            composable("settings") { SettingsScreen() }
            composable(
                route = "editTransaction?transactionId={transactionId}",
                arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: 0L
                EditTransactionScreen (transactionId = transactionId, navController = navController)
            }
            composable("allTransactions") { AllTransactionsScreen(navController) }
            composable(
                route = "transactionDetail?transactionId={transactionId}",
                arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: 0L
                TransactionDetailScreen(transactionId = transactionId, navController = navController)
            }

            composable(
                route = "collectionEdit/{collectionId}",
                arguments = listOf(navArgument("collectionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val collectionId = backStackEntry.arguments?.getLong("collectionId") ?: 0L
                CollectionEditScreen(
                    collectionId = collectionId,
                    navController = navController
                )
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)
