package com.satwik.oodapplication.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.satwik.oodapplication.presentation.admin.*
import com.satwik.oodapplication.presentation.auth.AuthViewModel
import com.satwik.oodapplication.presentation.auth.LoginScreen
import com.satwik.oodapplication.presentation.cook.CookDashboardScreen
import com.satwik.oodapplication.presentation.data_portal.DataPortalDashboardScreen
import com.satwik.oodapplication.presentation.manager.ManagerDashboardScreen
import com.satwik.oodapplication.presentation.student.MainStudentScreen
import com.satwik.oodapplication.utils.Constants

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            val viewModel: AuthViewModel = hiltViewModel()
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { role ->
                    val normalizedRole = role.trim().lowercase()
                    val route = when {
                        normalizedRole == Constants.ROLE_MANAGER || normalizedRole.contains("manager") -> Screen.ManagerGraph.route
                        normalizedRole == Constants.ROLE_ADMIN || normalizedRole.contains("admin") -> Screen.AdminGraph.route
                        normalizedRole == Constants.ROLE_COOK || normalizedRole.contains("cook") || normalizedRole.contains("prakesh") -> Screen.CookGraph.route
                        normalizedRole == Constants.ROLE_DATA_ENTRY || normalizedRole.contains("data") -> Screen.DataEntryGraph.route
                        else -> Screen.StudentGraph.route
                    }
                    navController.navigate(route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Manager Graph
        navigation(
            startDestination = Screen.ManagerDashboard.route,
            route = Screen.ManagerGraph.route
        ) {
            composable(Screen.ManagerDashboard.route) {
                ManagerDashboardScreen(
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.ManagerGraph.route) { inclusive = true }
                        }
                    }
                )
            }
        }

        // Admin Graph
        navigation(
            startDestination = Screen.AdminDashboard.route,
            route = Screen.AdminGraph.route
        ) {
            composable(Screen.AdminDashboard.route) {
                val viewModel: AdminViewModel = hiltViewModel()
                AdminDashboardScreen(
                    viewModel = viewModel,
                    onNavigateToMenu = { navController.navigate(Screen.AdminMenuManagement.route) },
                    onNavigateToStudents = { navController.navigate(Screen.AdminStudentManagement.route) },
                    onNavigateToNotifications = { navController.navigate(Screen.AdminNotifications.route) },
                    onNavigateToFoodCount = { navController.navigate(Screen.AdminFoodCount.route) },
                    onNavigateToAttendance = { navController.navigate(Screen.AdminAttendance.route) },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.AdminGraph.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.AdminMenuManagement.route) {
                val viewModel: MenuManagementViewModel = hiltViewModel()
                MenuManagementScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AdminStudentManagement.route) {
                StudentManagementScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AdminNotifications.route) {
                AdminNotificationsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AdminFoodCount.route) {
                AdminFoodCountScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AdminAttendance.route) {
                AdminAttendanceScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // Student Graph
        navigation(
            startDestination = Screen.StudentDashboard.route,
            route = Screen.StudentGraph.route
        ) {
            composable(Screen.StudentDashboard.route) {
                val viewModel: AuthViewModel = hiltViewModel()
                val userSession by viewModel.userSession.collectAsState()
                userSession?.let { 
                    MainStudentScreen(
                        user = it,
                        onLogout = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.StudentGraph.route) { inclusive = true }
                            }
                        }
                    ) 
                }
            }
        }

        // Cook Graph (Prakesh Portal)
        navigation(
            startDestination = "cook_dashboard",
            route = Screen.CookGraph.route
        ) {
            composable("cook_dashboard") {
                CookDashboardScreen(
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.CookGraph.route) { inclusive = true }
                        }
                    }
                )
            }
        }

        // Data Portal Graph
        navigation(
            startDestination = "data_portal_dashboard",
            route = Screen.DataEntryGraph.route
        ) {
            composable("data_portal_dashboard") {
                DataPortalDashboardScreen(
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.DataEntryGraph.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
