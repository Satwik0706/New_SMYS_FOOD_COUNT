package com.satwik.oodapplication.presentation.admin

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ContactPhone
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
import com.satwik.oodapplication.data.model.AdminWhatsAppConfig
import com.satwik.oodapplication.presentation.auth.AuthViewModel
import com.satwik.oodapplication.data.model.User
import com.satwik.oodapplication.ui.theme.SuccessGreen
import com.satwik.oodapplication.utils.Resource
import com.satwik.oodapplication.ui.components.AboutInfoDialog

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
    onNavigateToRequests: () -> Unit,
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
    var showWhatsAppSettings by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadAdminContact()
        visible = true
    }

    if (showAboutDialog) {
        AboutInfoDialog(onDismiss = { showAboutDialog = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            "Admin Control", 
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "System Overview & Controls",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAboutDialog = true }
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Info, 
                                    contentDescription = "App Info", 
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = {
                            authViewModel.logout()
                            onLogout()
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ExitToApp, 
                                    contentDescription = "Logout", 
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(800)) + slideInVertically(initialOffsetY = { 30 })
        ) {
            when (val resource = summaryResource) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(strokeWidth = 3.dp)
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Connection Error", fontWeight = FontWeight.Bold)
                            Text(resource.message ?: "Unknown error", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                is Resource.Success -> {
                    val summary = resource.data ?: return@AnimatedVisibility
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Today's Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatMiniCard(
                                label = "Students",
                                value = summary.totalStudents.toString(),
                                icon = Icons.Default.Group,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            StatMiniCard(
                                label = "Eating Today",
                                value = summary.countSubmitted.toString(),
                                icon = Icons.Default.Restaurant,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        SectionHeader("System Controls", "Manage student access")
                        
                        // Controls Container
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                ControlRow(
                                    title = "Student Portal Lock",
                                    subtitle = if (lockStatus.locked) "Portal is currently CLOSED" else "Portal is currently OPEN",
                                    icon = if (lockStatus.locked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    checked = lockStatus.locked,
                                    onCheckedChange = { viewModel.toggleLock() },
                                    activeColor = MaterialTheme.colorScheme.error
                                )
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                ControlRow(
                                    title = "Automation Engine",
                                    subtitle = if (lockStatus.automationEnabled) "Automatic scheduling ACTIVE" else "Manual override ACTIVE",
                                    icon = Icons.Default.SettingsSuggest,
                                    checked = lockStatus.automationEnabled,
                                    onCheckedChange = { viewModel.toggleAutomation() },
                                    activeColor = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        SectionHeader("Meal Management", "Individual schedule locks")
                        
                        // Meal Locks Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val mealLocks = listOf(
                                Triple("Breakfast", lockStatus.breakfastLocked, "breakfast"),
                                Triple("Lunch", lockStatus.lunchLocked, "lunch"),
                                Triple("Dinner", lockStatus.dinnerLocked, "dinner")
                            )
                            
                            mealLocks.forEach { (name, isLocked, type) ->
                                MealLockChip(
                                    name = name,
                                    isLocked = isLocked,
                                    onClick = { viewModel.toggleMealLock(type) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        
                        SectionHeader("Management Hub", "Tools and analytics")
                        
                        // Primary Actions Grid
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                HubCard("Students", "Directory", Icons.Default.People, MaterialTheme.colorScheme.primaryContainer, Modifier.weight(1f), onClick = onNavigateToStudents)
                                HubCard("Menu", "Meal Schedule", Icons.AutoMirrored.Filled.List, MaterialTheme.colorScheme.secondaryContainer, Modifier.weight(1f), onClick = onNavigateToMenu)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                HubCard("Food Count", "Analytics", Icons.Default.BarChart, MaterialTheme.colorScheme.tertiaryContainer, Modifier.weight(1f), onClick = onNavigateToFoodCount)
                                HubCard("Attendance", "Daily Logs", Icons.Default.AssignmentTurnedIn, MaterialTheme.colorScheme.surfaceVariant, Modifier.weight(1f), onClick = onNavigateToAttendance)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                HubCard("Requests", "Student Portal", Icons.Default.Update, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), Modifier.weight(1f), onClick = onNavigateToRequests)
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Secondary Actions List
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column {
                                ListItem(
                                    headlineContent = { Text("Snack Management", fontWeight = FontWeight.Bold) },
                                    supportingContent = { Text("Batch counts & lock control") },
                                    leadingContent = { Icon(Icons.Default.Fastfood, null, tint = MaterialTheme.colorScheme.primary) },
                                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
                                    modifier = Modifier.clickable { onNavigateToSnack() }
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                                ListItem(
                                    headlineContent = { Text("App Versions", fontWeight = FontWeight.Bold) },
                                    supportingContent = { Text("Check student install versions") },
                                    leadingContent = { Icon(Icons.Default.SystemUpdate, null, tint = MaterialTheme.colorScheme.secondary) },
                                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
                                    modifier = Modifier.clickable { showVersions = true }
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                                ListItem(
                                    headlineContent = { Text("WhatsApp Contacts", fontWeight = FontWeight.Bold) },
                                    supportingContent = { Text("Manage multiple admin numbers & sequence") },
                                    leadingContent = { Icon(Icons.Default.ContactPhone, null, tint = SuccessGreen) },
                                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
                                    modifier = Modifier.clickable { showWhatsAppSettings = true }
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                                val allLocksOff = viewModel.allLocksOff
                                ListItem(
                                    headlineContent = { 
                                        Text(
                                            "System Reset", 
                                            fontWeight = FontWeight.Bold,
                                            color = if (allLocksOff) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                                        ) 
                                    },
                                    supportingContent = { 
                                        Text(if (allLocksOff) "Force reset all daily counts" else "LOCKED: Unlock portal to reset") 
                                    },
                                    leadingContent = { 
                                        Icon(
                                            Icons.Default.Refresh, 
                                            null, 
                                            tint = if (allLocksOff) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                                        ) 
                                    },
                                    modifier = Modifier.clickable(enabled = allLocksOff) { showResetDialog = true }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Urgent Broadcast Button
                        Button(
                            onClick = onNavigateToNotifications,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Push Urgent Alert", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                        }
                        
                        Spacer(modifier = Modifier.height(40.dp))
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
                        LazyColumn {
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

        if (showWhatsAppSettings) {
            val configResource by viewModel.adminWhatsAppConfig.collectAsState()
            val currentNumbers = remember(configResource) {
                val initialList = if (configResource is Resource.Success) {
                    (configResource as Resource.Success).data?.numbers ?: emptyList()
                } else emptyList()
                mutableStateListOf<String>().apply { addAll(initialList) }
            }
            var selectedStrategy by remember(configResource) {
                mutableStateOf(
                    if (configResource is Resource.Success) {
                        (configResource as Resource.Success).data?.strategy ?: "SEQUENTIAL"
                    } else "SEQUENTIAL"
                )
            }
            var newNumberInput by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showWhatsAppSettings = false },
                title = { Text("Admin WhatsApp Contacts", fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
                        Text(
                            "Add multiple WhatsApp numbers. Student requests will rotate sequentially in order:",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Strategy Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = selectedStrategy == "SEQUENTIAL",
                                onClick = { selectedStrategy = "SEQUENTIAL" },
                                label = { Text("Sequential Rotation", style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = if (selectedStrategy == "SEQUENTIAL") {
                                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                            FilterChip(
                                selected = selectedStrategy == "RANDOM",
                                onClick = { selectedStrategy = "RANDOM" },
                                label = { Text("Random Pick", style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = if (selectedStrategy == "RANDOM") {
                                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Input for new number
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newNumberInput,
                                onValueChange = { newNumberInput = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("e.g. 919876543210") },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            IconButton(
                                onClick = {
                                    val trimmed = newNumberInput.trim()
                                    if (trimmed.isNotEmpty() && !currentNumbers.contains(trimmed)) {
                                        currentNumbers.add(trimmed)
                                        newNumberInput = ""
                                    }
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.AddCircle, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Active Admin Numbers (${currentNumbers.size}):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            itemsIndexed(currentNumbers.toList()) { index, num ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${index + 1}. $num", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        IconButton(
                                            onClick = {
                                                currentNumbers.removeAt(index)
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.saveWhatsAppConfig(
                                AdminWhatsAppConfig(
                                    numbers = currentNumbers.toList(),
                                    strategy = selectedStrategy
                                )
                            )
                            showWhatsAppSettings = false
                        }
                    ) {
                        Text("Save Config")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showWhatsAppSettings = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun StatMiniCard(
    label: String, 
    value: String, 
    icon: ImageVector, 
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = color.copy(alpha = 0.8f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun ControlRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    activeColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = if (checked) activeColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon, 
                    null, 
                    tint = if (checked) activeColor else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = activeColor,
                checkedTrackColor = activeColor.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun MealLockChip(
    name: String,
    isLocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isLocked) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
    val contentColor = if (isLocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (isLocked) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    null,
                    modifier = Modifier.size(14.dp),
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = contentColor)
            }
        }
    }
}

@Composable
fun HubCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor.copy(alpha = 0.15f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, containerColor.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                color = containerColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, modifier = Modifier.size(20.dp), tint = containerColor)
                }
            }
            Column {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
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
