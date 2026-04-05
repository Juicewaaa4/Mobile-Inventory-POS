package com.example.mobileinventory.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobileinventory.viewmodel.MainViewModel

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController, viewModel) }
        composable("admin_dashboard") { AdminDashboardScreen(navController, viewModel) }
        composable("admin_sales_today") { AdminSalesTodayScreen(navController, viewModel) }
        composable("cashier_dashboard") { CashierPosScreen(navController, viewModel) }
        composable("add_account") { AddAccountScreen(navController, viewModel) }
    }
}
