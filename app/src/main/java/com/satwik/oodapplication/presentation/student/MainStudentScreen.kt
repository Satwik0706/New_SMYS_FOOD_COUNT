package com.satwik.oodapplication.presentation.student

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.satwik.oodapplication.data.model.User
import com.satwik.oodapplication.presentation.admin.MenuManagementViewModel
import com.satwik.oodapplication.presentation.auth.AuthViewModel

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainStudentScreen(
    user: User,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val items = listOf("Home", "Menu", "Attendance", "Alerts")
    val routes = listOf("Home", "Menu", "Count", "Notifications")
    val icons = listOf(Icons.Default.Home, Icons.Default.ShoppingCart, Icons.Default.DateRange, Icons.Default.Notifications)
    var selectedItem by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "SMYS Portal", 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                    ) 
                },
                actions = {
                    IconButton(onClick = {
                        authViewModel.logout()
                        onLogout()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                icons[index], 
                                contentDescription = item,
                                tint = if (selectedItem == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            ) 
                        },
                        label = { 
                            Text(
                                item, 
                                fontWeight = if (selectedItem == index) FontWeight.Bold else FontWeight.Medium
                            ) 
                        },
                        selected = selectedItem == index,
                        onClick = {
                            if (selectedItem != index) {
                                selectedItem = index
                                navController.navigate(routes[index]) {
                                    popUpTo("Home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "Home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("Home") { StudentHomeScreen(user.name, user.year ?: "") }
            composable("Menu") { 
                val viewModel: MenuManagementViewModel = hiltViewModel()
                StudentMenuScreen(viewModel)
            }
            composable("Count") { 
                val viewModel: StudentFoodCountViewModel = hiltViewModel()
                StudentFoodCountScreen(viewModel, user.uid)
            }
            composable("Notifications") { 
                StudentNotificationsScreen(user.year ?: "")
            }
        }
    }
}
