package com.satwik.oodapplication.presentation.admin

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.satwik.oodapplication.utils.Constants
import com.satwik.oodapplication.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAttendanceScreen(
    viewModel: AdminAttendanceViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val students by viewModel.students.collectAsState()
    val attendanceMap by viewModel.attendanceMap.collectAsState()
    val foodCounts by viewModel.foodCounts.collectAsState()
    val missedFoodStudents by viewModel.missedFoodStudents.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    var selectedBatch by remember { mutableStateOf("All") }
    var filterNotOnLeave by remember { mutableStateOf(false) }
    var filterDinnerOrdered by remember { mutableStateOf(false) }
    var sortByBatch by remember { mutableStateOf(false) }

    val batches = listOf("All") + students.mapNotNull { it.year }.distinct().sorted()
    
    var showResultDialog by remember { mutableStateOf(false) }

    LaunchedEffect(missedFoodStudents) {
        if (missedFoodStudents.isNotEmpty()) {
            showResultDialog = true
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Daily Attendance", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            // Batch Filter
            ScrollableTabRow(
                selectedTabIndex = batches.indexOf(selectedBatch),
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                batches.forEach { batch ->
                    Tab(
                        selected = selectedBatch == batch,
                        onClick = { selectedBatch = batch },
                        text = { Text(batch, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterNotOnLeave,
                    onClick = { filterNotOnLeave = !filterNotOnLeave },
                    label = { Text("Not on Leave", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = if (filterNotOnLeave) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
                FilterChip(
                    selected = filterDinnerOrdered,
                    onClick = { filterDinnerOrdered = !filterDinnerOrdered },
                    label = { Text("Dinner Ordered", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = if (filterDinnerOrdered) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
                FilterChip(
                    selected = sortByBatch,
                    onClick = { sortByBatch = !sortByBatch },
                    label = { Text("Sort by Batch", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = if (sortByBatch) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val displayStudents = remember(students, selectedBatch, filterNotOnLeave, filterDinnerOrdered, sortByBatch, foodCounts) {
                var filtered = if (selectedBatch == "All") students else students.filter { it.year == selectedBatch }
                
                if (filterNotOnLeave) {
                    filtered = filtered.filter { student ->
                        val count = foodCounts[student.uid]
                        val onLeave = count?.isOnLeave ?: student.isLeave
                        !onLeave
                    }
                }
                
                if (filterDinnerOrdered) {
                    filtered = filtered.filter { student ->
                        val count = foodCounts[student.uid]
                        if (count != null) {
                            !count.isOnLeave && count.isDinner
                        } else {
                            student.dinnerPref
                        }
                    }
                }
                
                if (sortByBatch) {
                    filtered = filtered.sortedBy { it.year }
                }
                
                filtered
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(
                    items = displayStudents,
                    key = { it.uid }
                ) { student ->
                    AttendanceCard(
                        name = student.name,
                        roll = student.rollNumber ?: "",
                        isPresent = attendanceMap[student.uid] ?: true,
                        onToggle = { viewModel.toggleAttendance(student.uid) }
                    )
                }
            }

            Button(
                onClick = { viewModel.submitAttendance() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (uiState is Resource.Loading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Submit Attendance", fontWeight = FontWeight.ExtraBold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = { 
                showResultDialog = false
                viewModel.acknowledgeReport()
            },
            title = { Text("Food Mismanagement Report", fontWeight = FontWeight.ExtraBold) },
            text = {
                Column {
                    Text("The following students ordered dinner but were marked as Absent:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    missedFoodStudents.forEach { student ->
                        Text("• ${student.name} (${student.year})", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("These students will be reverted to 'Present' for the final submission.", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(onClick = { 
                    showResultDialog = false
                    viewModel.acknowledgeReport()
                }) {
                    Text("Acknowledge & Revert")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun AttendanceCard(name: String, roll: String, isPresent: Boolean, onToggle: () -> Unit) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
        onClick = onToggle,
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isPresent) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    Text(roll, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                
                StatusBadge(if (isPresent) "Present" else "Absent")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                if (isPresent) "Tap to mark Absent" else "Tap to mark Present",
                style = MaterialTheme.typography.labelSmall,
                color = if (isPresent) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "Present" -> Color(0xFF4CAF50) // Keep standard success green or use SuccessGreen if imported
        else -> MaterialTheme.colorScheme.error
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Text(
            status, 
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
