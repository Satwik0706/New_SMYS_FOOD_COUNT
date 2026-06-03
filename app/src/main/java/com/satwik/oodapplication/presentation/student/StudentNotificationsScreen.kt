package com.satwik.oodapplication.presentation.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.satwik.oodapplication.utils.Resource

@Composable
fun StudentNotificationsScreen(
    studentYear: String,
    viewModel: StudentHomeViewModel = hiltViewModel()
) {
    val notificationState by viewModel.notifications.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("All Notifications", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        when (val state = notificationState) {
            is Resource.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is Resource.Error -> Text("Error: ${state.message}")
            is Resource.Success -> {
                val list = state.data?.filter { it.targetYear == "All" || it.targetYear == studentYear } ?: emptyList()
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(list) { notification ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(notification.title, style = MaterialTheme.typography.titleMedium)
                                Text(notification.body, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
