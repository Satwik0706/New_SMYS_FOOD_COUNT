package com.satwik.oodapplication.presentation.admin

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.satwik.oodapplication.presentation.auth.AuthViewModel
import com.satwik.oodapplication.data.model.User
import com.satwik.oodapplication.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onNavigateToMenu: () -> Unit,
    onNavigateToStudents: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToFoodCount: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToSnack: () -> Unit,
    onLogout: () -> Unit
) {
    val summaryResource by viewModel.summary.collectAsState()
    val lockStatus by viewModel.lockStatus.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    
    var visible by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetType by remember { mutableStateOf("All") }
    var isChecked by remember { mutableStateOf(false) }
    var showVersions by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Administrator", 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                    ) 
                },
                actions = {
                    IconButton(onClick = {
                        authViewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(700)) + slideInVertically(initialOffsetY = { 40 })
        ) {
            when (val resource = summaryResource) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${resource.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is Resource.Success -> {
                    val summary = resource.data ?: return@AnimatedVisibility
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Overview",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Key system metrics for today",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            AdminStatCard("Total Enrollment", summary.totalStudents.toString(), Icons.Default.Person, Modifier.weight(1f))
                            AdminStatCard("Active Today", summary.presentStudents.toString(), Icons.Default.CheckCircle, Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = if (lockStatus.locked) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f) 
                                                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Student Portal Lock", 
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (lockStatus.locked) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = if (lockStatus.locked) "Submissions are currently disabled" else "Students can submit food counts",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (lockStatus.locked) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                                Switch(
                                    checked = lockStatus.locked,
                                    onCheckedChange = { viewModel.toggleLock() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.onError,
                                        checkedTrackColor = MaterialTheme.colorScheme.error
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Automation Card
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = if (lockStatus.automationEnabled) MaterialTheme.colorScheme.secondaryContainer 
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Auto-Lock System", 
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (lockStatus.automationEnabled) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (lockStatus.automationEnabled) "Scheduled: B (5 AM), L (9 AM), D (4:30 PM)\nCycle: Resets 8 PM Daily" else "System is in manual mode",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (lockStatus.automationEnabled) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                                Switch(
                                    checked = lockStatus.automationEnabled,
                                    onCheckedChange = { viewModel.toggleAutomation() }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Individual Meal Locks",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .padding(8.dp)
                        ) {
                            val meals = listOf(
                                Triple("Breakfast", lockStatus.breakfastLocked, "breakfast"),
                                Triple("Lunch", lockStatus.lunchLocked, "lunch"),
                                Triple("Dinner", lockStatus.dinnerLocked, "dinner")
                            )

                            meals.forEach { (name, isLocked, type) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(name, fontWeight = FontWeight.Bold)
                                    Switch(
                                        checked = isLocked,
                                        onCheckedChange = { viewModel.toggleMealLock(type) },
                                        modifier = Modifier.scale(0.8f),
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = MaterialTheme.colorScheme.error,
                                            checkedTrackColor = MaterialTheme.colorScheme.errorContainer
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            "System Management", 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        AdminActionCard("Student Directory", "Manage enrollments and data", Icons.Default.AccountCircle, onClick = onNavigateToStudents)
                        AdminActionCard("Daily Attendance", "Mark absent/leave & check food", Icons.Default.CheckCircle, onClick = onNavigateToAttendance)
                        AdminActionCard("App Usage Details", "View student app versions", Icons.Default.Info, onClick = { showVersions = true })
                        AdminActionCard("Food Requirements", "Daily meal counts & analytics", Icons.Default.Info, onClick = onNavigateToFoodCount)
                        AdminActionCard("Mess Menu", "Update daily meal schedule", Icons.AutoMirrored.Filled.List, onClick = onNavigateToMenu)
                        AdminActionCard("Snack Management", "Lock snacks & batch counts", Icons.Default.Fastfood, onClick = onNavigateToSnack)
                        AdminActionCard("Notification Center", "Broadcast to all students", Icons.Default.Notifications, onClick = onNavigateToNotifications)
                        val allLocksOff = viewModel.allLocksOff
                        AdminActionCard(
                            title = "Force Reset All Counts", 
                            subtitle = if (allLocksOff) "Set all non-leave student counts to 0" else "LOCKED: Unlock all portal/meal locks to reset", 
                            icon = Icons.Default.Refresh,
                            color = if (allLocksOff) null else MaterialTheme.colorScheme.error,
                            onClick = { showResetDialog = true }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Quick Trigger Button
                        Button(
                            onClick = { onNavigateToNotifications() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Push Urgent Alert", fontWeight = FontWeight.ExtraBold)
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }

        if (showResetDialog) {
            val allLocksOff = viewModel.allLocksOff
            AlertDialog(
                onDismissRequest = { 
                    showResetDialog = false 
                    isChecked = false
                    resetType = "All"
                },
                title = { Text("Force Reset Meal Counts", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        if (!allLocksOff) {
                            Text(
                                "ERROR: All locks must be OFF to perform a reset. Please unlock the portal and all meals first.",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        Text("Choose which meal counts to reset to ZERO for students NOT on leave:", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(12.dp))

                        val options = listOf("All", "Breakfast", "Lunch", "Snack", "Dinner")
                        options.forEach { option ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { resetType = option },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = resetType == option, onClick = { resetType = option })
                                Text(option)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("This action cannot be undone.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { isChecked = it },
                                enabled = allLocksOff
                            )
                            Text("Confirm selection reset", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (resetType == "All") {
                                viewModel.resetAllCounts()
                            } else {
                                viewModel.resetMeal(resetType)
                            }
                            showResetDialog = false
                            isChecked = false
                        },
                        enabled = isChecked && allLocksOff,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Reset Now")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showResetDialog = false 
                        isChecked = false
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showVersions) {
            AlertDialog(
                onDismissRequest = { showVersions = false },
                title = { Text("Student App Versions", fontWeight = FontWeight.Black) },
                text = {
                    Box(modifier = Modifier.heightIn(max = 400.dp)) {
                        androidx.compose.foundation.lazy.LazyColumn {
                            items(allStudents.sortedBy { it.name }) { student ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        student.name, 
                                        modifier = Modifier.weight(1f), 
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        color = if (student.appVersion != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = student.appVersion ?: "N/A",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            color = if (student.appVersion != null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showVersions = false }) { Text("Close") }
                }
            )
        }
    }
}

@Composable
fun AdminStatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    OutlinedCard(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun AdminActionCard(
    title: String, 
    subtitle: String, 
    icon: ImageVector, 
    color: Color? = null,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = color?.copy(alpha = 0.05f) ?: MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            2.dp, 
            color ?: MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = color?.copy(alpha = 0.2f) ?: MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color ?: MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = color ?: Color.Unspecified)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = color?.copy(alpha = 0.7f) ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = color ?: MaterialTheme.colorScheme.onSurface)
        }
    }
}
