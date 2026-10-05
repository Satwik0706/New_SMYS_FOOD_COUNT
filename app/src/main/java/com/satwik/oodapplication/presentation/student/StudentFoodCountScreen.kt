package com.satwik.oodapplication.presentation.student

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.satwik.oodapplication.presentation.common.components.shimmerModifier
import com.satwik.oodapplication.utils.vibrateSensible
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.satwik.oodapplication.data.model.User
import com.satwik.oodapplication.ui.theme.*
import com.satwik.oodapplication.utils.Resource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StudentFoodCountScreen(
    viewModel: StudentFoodCountViewModel,
    user: User
) {
    val studentId = user.uid
    val foodCountState by viewModel.foodCountState.collectAsState()
    val lockStatusState by viewModel.lockStatus.collectAsState()
    val snackStatusState by viewModel.snackStatus.collectAsState()
    val studentRequest by viewModel.studentRequest.collectAsState()
    val adminWhatsApp by viewModel.adminWhatsApp.collectAsState()
    val requestActionState by viewModel.requestActionState.collectAsState()
    
    val context = LocalContext.current
    var visible by remember { mutableStateOf(false) }
    var showRequestDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { 
        viewModel.loadData(studentId)
        visible = true 
    }

    LaunchedEffect(requestActionState) {
        if (requestActionState is Resource.Success) {
            viewModel.resetRequestState()
        }
    }

    val lockData = (lockStatusState as? Resource.Success)?.data
    val snackLockData = (snackStatusState as? Resource.Success)?.data
    val isMasterLocked = lockData?.locked ?: false
    
    val isAnyMainMealLocked = lockData?.let { 
        it.breakfastLocked || it.lunchLocked || it.dinnerLocked 
    } ?: false

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(800)) + slideInVertically(initialOffsetY = { 20 })
    ) {
        when (val state = foodCountState) {
            is Resource.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(40.dp).shimmerModifier())
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp).shimmerModifier())
                    Spacer(modifier = Modifier.height(32.dp))
                    repeat(4) {
                        Box(modifier = Modifier.fillMaxWidth().height(60.dp).padding(vertical = 4.dp).shimmerModifier())
                    }
                }
            }
            is Resource.Success -> {
                val data = state.data ?: return@AnimatedVisibility
                val canToggleLunchBox = !isMasterLocked && !data.isOnLeave && !(lockData?.breakfastLocked ?: false)
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    HeaderSection("Meal Attendance", "Date: ${data.date}")

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isMasterLocked) {
                        StatusAlertCard(
                            message = "The submission window is currently closed.",
                            icon = Icons.Default.Lock,
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    } else if (data.isOnLeave) {
                        StatusAlertCard(
                            message = "You are currently ON LEAVE. All meals are locked.",
                            icon = Icons.Default.Info,
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    } else if (isAnyMainMealLocked) {
                        StatusAlertCard(
                            message = "Main meals are finalized. Changes are disabled.",
                            icon = Icons.Default.Info,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    SectionHeader("Today's Schedule", "Select the meals you'll attend")
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            MealToggleItem(
                                label = "Breakfast",
                                subtitle = "07:30 AM - 09:00 AM",
                                isSelected = data.isBreakfast,
                                color = BreakfastColor,
                                enabled = !isMasterLocked && !data.isOnLeave && !(lockData?.breakfastLocked ?: false),
                                locked = lockData?.breakfastLocked ?: false,
                                onToggle = { 
                                    context.vibrateSensible()
                                    viewModel.toggleMeal(studentId, data.date, "breakfast") 
                                }
                            )
                            
                            // Lunch Box Sub-Toggle
                            AnimatedVisibility(visible = data.isBreakfast) {
                                Surface(
                                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (data.isLunchBox) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                                    onClick = { 
                                        if (canToggleLunchBox) {
                                            context.vibrateSensible()
                                            viewModel.toggleMeal(studentId, data.date, "lunchbox")
                                        }
                                    },
                                    enabled = canToggleLunchBox
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Info, 
                                                null, 
                                                modifier = Modifier.size(16.dp),
                                                tint = if (data.isLunchBox) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text("Lunch Box", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                Text("Carry-away meal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                            }
                                        }
                                        Switch(
                                            checked = data.isLunchBox,
                                            onCheckedChange = null,
                                            enabled = canToggleLunchBox,
                                            modifier = Modifier.scale(0.7f)
                                        )
                                    }
                                }
                            }

                            MealToggleItem(
                                label = "Lunch",
                                subtitle = if (data.isLunchBox) "Lunch Box Active" else "12:30 PM - 02:00 PM",
                                isSelected = data.isLunch,
                                color = LunchColor,
                                enabled = !isMasterLocked && !data.isOnLeave && !(lockData?.lunchLocked ?: false) && !data.isLunchBox,
                                locked = lockData?.lunchLocked ?: false,
                                onToggle = { 
                                    context.vibrateSensible()
                                    viewModel.toggleMeal(studentId, data.date, "lunch") 
                                }
                            )
                            MealToggleItem(
                                label = "Snacks",
                                subtitle = "04:30 PM - 05:30 PM",
                                isSelected = data.isSnack,
                                color = SnackColor,
                                enabled = !isMasterLocked && !data.isOnLeave && !(snackLockData?.locked ?: false),
                                locked = snackLockData?.locked ?: false,
                                onToggle = { 
                                    context.vibrateSensible()
                                    viewModel.toggleMeal(studentId, data.date, "snack") 
                                }
                            )
                            MealToggleItem(
                                label = "Dinner",
                                subtitle = "07:30 PM - 09:00 PM",
                                isSelected = data.isDinner,
                                color = DinnerColor,
                                enabled = !isMasterLocked && !data.isOnLeave && !(lockData?.dinnerLocked ?: false),
                                locked = lockData?.dinnerLocked ?: false,
                                onToggle = { 
                                    context.vibrateSensible()
                                    viewModel.toggleMeal(studentId, data.date, "dinner") 
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    SectionHeader("Availability", "Mark your leave status")
                    
                    val requestData = (studentRequest as? Resource.Success)?.data
                    val hasPendingRequest = requestData?.status == "PENDING"
                    
                    AvailabilityCard(
                        title = "On Leave / Unavailable",
                        subtitle = when {
                            hasPendingRequest -> "LOCKED: Pending admin approval"
                            isAnyMainMealLocked -> "Locked: Meals are finalized"
                            else -> "Auto-reset counts to 0"
                        },
                        isSelected = data.isOnLeave,
                        enabled = !isMasterLocked && !isAnyMainMealLocked && !hasPendingRequest,
                        onToggle = { 
                            context.vibrateSensible()
                            viewModel.toggleMeal(studentId, data.date, "leave") 
                        },
                        color = MaterialTheme.colorScheme.error
                    )
                    
                    // REQUEST PORTAL SECTION
                    if (data.isOnLeave && isAnyMainMealLocked && !isMasterLocked) {
                        Spacer(modifier = Modifier.height(24.dp))
                        SectionHeader("Missed Count?", "Request admin to mark you present")
                        
                        MissedCountRequestCard(
                            request = requestData,
                            onSendRequest = { showRequestDialog = true },
                            onWhatsAppRedirect = {
                                viewModel.getRedirectWhatsAppNumber { number ->
                                    if (number.isNotEmpty()) {
                                        val text = "Hi Admin, I (${user.name}) missed my food count for today (${data.date}). I've sent a request in the app. Please approve it. Thanks!"
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                                        intent.data = android.net.Uri.parse("https://wa.me/$number?text=${android.net.Uri.encode(text)}")
                                        context.startActivity(intent)
                                    }
                                }
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Connection issue. Please try again.", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showRequestDialog) {
        var b by remember { mutableStateOf(true) }
        var l by remember { mutableStateOf(true) }
        var d by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showRequestDialog = false },
            title = { Text("Request Food Count", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Select meals you want to attend today:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    MealCheckRow("Breakfast", b) { b = it }
                    MealCheckRow("Lunch", l) { l = it }
                    MealCheckRow("Dinner", d) { d = it }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Note: Admin must approve this request before your status changes.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitRequest(user, b, l, d)
                        showRequestDialog = false
                    },
                    enabled = b || l || d
                ) {
                    Text("Submit Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRequestDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MealCheckRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun MissedCountRequestCard(
    request: com.satwik.oodapplication.data.model.FoodRequest?,
    onSendRequest: () -> Unit,
    onWhatsAppRedirect: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            if (request == null) {
                Text(
                    "Forgot to mark yourself as Present?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Since the portal is locked, you can send a request to the admin for manual approval.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onSendRequest,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Update, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Request to Admin")
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = when (request.status) {
                            "PENDING" -> MaterialTheme.colorScheme.secondaryContainer
                            "APPROVED" -> SuccessGreen.copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.errorContainer
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = request.status,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = when (request.status) {
                                "PENDING" -> MaterialTheme.colorScheme.onSecondaryContainer
                                "APPROVED" -> SuccessGreen
                                else -> MaterialTheme.colorScheme.error
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
                    Text("Request sent at ${timeFormatter.format(Date(request.timestamp))}", style = MaterialTheme.typography.labelSmall)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = when (request.status) {
                        "PENDING" -> "Your request is waiting for admin approval. You can also message them on WhatsApp."
                        "APPROVED" -> "Admin approved your request. Your status has been updated."
                        else -> "Request rejected: ${request.adminNote ?: "No reason provided"}"
                    },
                    style = MaterialTheme.typography.bodySmall
                )

                if (request.status == "PENDING") {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onWhatsAppRedirect,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SuccessGreen),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send to WhatsApp")
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection(title: String, subtitle: String) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = (-0.5).sp
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun StatusAlertCard(
    message: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
        shape = RoundedCornerShape(20.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = contentColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                message,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AvailabilityCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
    color: Color? = null
) {
    Card(
        onClick = { if (enabled) onToggle() },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) (color?.copy(alpha = 0.05f) ?: MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.05f))
                            else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (isSelected) (color?.copy(alpha = 0.4f) ?: MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) (color ?: MaterialTheme.colorScheme.onSurface) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Switch(
                checked = isSelected,
                onCheckedChange = null,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = color ?: MaterialTheme.colorScheme.primary,
                    checkedTrackColor = (color ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
fun MealToggleItem(
    label: String,
    subtitle: String,
    isSelected: Boolean,
    color: Color,
    enabled: Boolean,
    locked: Boolean = false,
    onToggle: () -> Unit
) {
    Surface(
        onClick = { if (enabled) onToggle() },
        color = Color.Transparent,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    color = if (locked) MaterialTheme.colorScheme.surfaceVariant else color.copy(alpha = if (isSelected) 1f else 0.1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (locked) {
                            Icon(Icons.Default.Lock, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
                        } else {
                            Text(
                                label.take(1), 
                                style = MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else color
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (enabled) MaterialTheme.colorScheme.onSurface 
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        if (locked) "Locked by Admin" else subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            Switch(
                checked = isSelected,
                onCheckedChange = null,
                enabled = enabled,
                modifier = Modifier.scale(0.8f),
                thumbContent = if (isSelected) {
                    {
                        Icon(
                            imageVector = if (locked) Icons.Default.Lock else Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                } else null
            )
        }
    }
}
