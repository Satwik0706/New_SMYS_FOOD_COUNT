package com.satwik.oodapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.satwik.oodapplication.presentation.auth.AuthViewModel
import com.satwik.oodapplication.presentation.common.Screen
import com.satwik.oodapplication.presentation.common.SetupNavGraph
import com.satwik.oodapplication.ui.theme.OodapplicationTheme
import com.satwik.oodapplication.utils.Constants
import com.satwik.oodapplication.worker.FoodReminderWorker
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        askNotificationPermission()

        // Start daily 9:30 PM reminder
        FoodReminderWorker.scheduleNext(applicationContext)

        // Subscribe all users to the general announcements topic
        FirebaseMessaging.getInstance().subscribeToTopic("all_students")

        setContent {
            OodapplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authViewModel: AuthViewModel = hiltViewModel()
                    val userSession by authViewModel.userSession.collectAsState()
                    val navController = rememberNavController()
                    
                    val startDestination = if (userSession != null) {
                        when (userSession?.role) {
                            Constants.ROLE_MANAGER -> Screen.ManagerGraph.route
                            Constants.ROLE_ADMIN -> Screen.AdminGraph.route
                            Constants.ROLE_STUDENT -> Screen.StudentGraph.route
                            Constants.ROLE_COOK -> Screen.CookGraph.route
                            Constants.ROLE_DATA_ENTRY -> Screen.DataEntryGraph.route
                            else -> Screen.Login.route
                        }
                    } else {
                        Screen.Login.route
                    }
                    
                    SetupNavGraph(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}
