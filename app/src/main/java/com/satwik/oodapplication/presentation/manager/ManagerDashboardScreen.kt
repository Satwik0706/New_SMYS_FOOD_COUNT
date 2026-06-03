package com.satwik.oodapplication.presentation.manager

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.satwik.oodapplication.data.model.AuditLog
import com.satwik.oodapplication.data.model.User
import com.satwik.oodapplication.presentation.auth.AuthViewModel
import com.satwik.oodapplication.utils.Constants
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerDashboardScreen(
    viewModel: ManagerViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onLogout: () -> Unit
) {
    val stats by viewModel.stats.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val logs by viewModel.logs.collectAsState()
    var showResetDialog by remember { mutableStateOf<User?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Manager Hub", 
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
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text("Insights") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    label = { Text("Security") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Audit Logs") }
                )
            }
        }
    ) { padding ->
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 30 })
        ) {
            Box(modifier = Modifier.padding(padding)) {
                when (selectedTab) {
                    0 -> InsightsTab(stats, onNavigateToStudents = { selectedTab = 1 })
                    1 -> SecurityTab(
                        allUsers = allUsers, 
                        onReset = { showResetDialog = it },
                        viewModel = viewModel
                    )
                    2 -> LogsTab(logs)
                }
            }
        }

        showResetDialog?.let { user ->
            var newPassword by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showResetDialog = null },
                shape = RoundedCornerShape(28.dp),
                title = { Text("Secure Reset", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Resetting password for ${user.name}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("New Secure Password") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newPassword.isNotBlank()) {
                                viewModel.resetUserPassword(user.uid, newPassword)
                                showResetDialog = null
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirm Update")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = null }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun InsightsTab(stats: ManagerStats, onNavigateToStudents: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "System Performance",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Real-time overview of application usage",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ManagerStatCard(
                label = "Total Accounts", 
                value = (stats.adminCount + stats.studentCount).toString(), 
                icon = Icons.Default.AccountCircle, 
                modifier = Modifier.weight(1f).clickable { onNavigateToStudents() }
            )
            ManagerStatCard("Live Submissions", stats.submittedToday.toString(), Icons.Default.CheckCircle, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        Text("Role Distribution", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                StatRow("Administrators", stats.adminCount.toString(), MaterialTheme.colorScheme.primary)
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
                StatRow("Students", stats.studentCount.toString(), MaterialTheme.colorScheme.secondary)
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
                StatRow("Total Reach", (stats.adminCount + stats.studentCount).toString(), MaterialTheme.colorScheme.tertiary)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SecurityTab(allUsers: List<User>, onReset: (User) -> Unit, viewModel: ManagerViewModel) {
    var selectedBatch by remember { mutableStateOf("All") }
    val batches = remember(allUsers) { 
        listOf("All") + allUsers.mapNotNull { it.year }.distinct().sorted() 
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Security Controls",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
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
        
        Spacer(modifier = Modifier.height(16.dp))

        val filteredUsers = remember(allUsers, selectedBatch) {
            val list = if (selectedBatch == "All") allUsers else allUsers.filter { it.year == selectedBatch }
            list.sortedBy { it.name }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(filteredUsers) { user ->
                UserSecurityCard(user, onReset = { onReset(user) }, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun LogsTab(logs: List<AuditLog>) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Audit Logs",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Tracking system activities (Self-deletes every 2 days)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(logs) { log ->
                LogCard(log)
            }
        }
    }
}

@Composable
fun LogCard(log: AuditLog) {
    val date = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(log.timestamp))
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(log.action, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyLarge)
                Text("User: ${log.userName}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
            Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun UserSecurityCard(user: User, onReset: () -> Unit, viewModel: ManagerViewModel) {
    var showFoodEdit by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                user.name.take(1).uppercase(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(user.name, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${user.role.uppercase()} • ${user.email}", 
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Row {
                    if (user.role == Constants.ROLE_STUDENT) {
                        IconButton(
                            onClick = { showFoodEdit = true },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.List, contentDescription = "Edit Food", modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    
                    IconButton(
                        onClick = onReset,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Reset", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }

    if (showFoodEdit) {
        ManagerFoodEditDialog(
            user = user,
            onDismiss = { showFoodEdit = false },
            onUpdate = { updatedUser ->
                viewModel.updateStudentPreferences(updatedUser)
                showFoodEdit = false
            }
        )
    }
}

@Composable
fun ManagerFoodEditDialog(user: User, onDismiss: () -> Unit, onUpdate: (User) -> Unit) {
    var b by remember { mutableStateOf(user.breakfastPref) }
    var l by remember { mutableStateOf(user.lunchPref) }
    var s by remember { mutableStateOf(user.snackPref) }
    var d by remember { mutableStateOf(user.dinnerPref) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Override Preferences", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Modify permanent defaults for ${user.name}", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(16.dp))
                PreferenceToggle("Breakfast", b) { b = it }
                PreferenceToggle("Lunch", l) { l = it }
                PreferenceToggle("Snack", s) { s = it }
                PreferenceToggle("Dinner", d) { d = it }
            }
        },
        confirmButton = {
            Button(onClick = { 
                onUpdate(user.copy(breakfastPref = b, lunchPref = l, snackPref = s, dinnerPref = d)) 
            }) { Text("Update Defaults") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun PreferenceToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontWeight = FontWeight.Bold)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun ManagerStatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    OutlinedCard(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(value, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun StatRow(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
        Text(
            value, 
            style = MaterialTheme.typography.titleMedium, 
            fontWeight = FontWeight.Bold, 
            color = MaterialTheme.colorScheme.primary
        )
    }
}
