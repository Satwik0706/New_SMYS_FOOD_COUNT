package com.satwik.oodapplication.presentation.student

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.satwik.oodapplication.data.model.FoodCount
import com.satwik.oodapplication.data.model.LockStatus
import com.satwik.oodapplication.ui.theme.*
import com.satwik.oodapplication.utils.Resource

@Composable
fun StudentFoodCountScreen(
    viewModel: StudentFoodCountViewModel,
    studentId: String
) {
    val foodCountState by viewModel.foodCountState.collectAsState()
    val lockStatusState by viewModel.lockStatus.collectAsState()
    
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { 
        viewModel.loadData(studentId)
        visible = true 
    }

    val lockData = (lockStatusState as? Resource.Success)?.data
    val isMasterLocked = lockData?.locked ?: false
    
    val isAnyMainMealLocked = lockData?.let { 
        it.breakfastLocked || it.lunchLocked || it.dinnerLocked 
    } ?: false

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { 20 })
    ) {
        when (val state = foodCountState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(strokeWidth = 3.dp)
                }
            }
            is Resource.Success -> {
                val data = state.data!!
                val canToggleLunchBox = !isMasterLocked && !data.isLeave && !(lockData?.breakfastLocked ?: false)
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "Meal Attendance",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Confirm your presence for today's meals",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    if (isMasterLocked) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "The submission window is currently closed.",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    } else if (data.isLeave) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "You are ON LEAVE. All meals are locked until you toggle it OFF below.",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    } else if (isAnyMainMealLocked && !isMasterLocked) {
                         Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Main meals are locked. You cannot change leave status now.",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Text("Today's Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            MealToggleItem(
                                label = "Breakfast",
                                subtitle = "Morning Session",
                                isSelected = data.breakfast,
                                color = BreakfastColor,
                                enabled = !isMasterLocked && !data.isLeave && !(lockData?.breakfastLocked ?: false),
                                onToggle = { viewModel.toggleMeal(studentId, data.date, "breakfast") }
                            )
                            
                            // Lunch Box Sub-Toggle
                            AnimatedVisibility(visible = data.breakfast) {
                                Surface(
                                    onClick = { if (canToggleLunchBox) viewModel.toggleMeal(studentId, data.date, "lunchbox") },
                                    enabled = canToggleLunchBox,
                                    color = Color.Transparent
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 64.dp, end = 12.dp, bottom = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Lunch Box", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text("Pick up during breakfast", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                        }
                                        Switch(
                                            checked = data.lunchBox,
                                            onCheckedChange = null,
                                            enabled = canToggleLunchBox,
                                            modifier = Modifier.scale(0.8f)
                                        )
                                    }
                                }
                            }

                            MealToggleItem(
                                label = "Lunch",
                                subtitle = if (data.lunchBox) "Lunch Box Selected" else "Afternoon Session",
                                isSelected = data.lunch,
                                color = LunchColor,
                                enabled = !isMasterLocked && !data.isLeave && !(lockData?.lunchLocked ?: false) && !data.lunchBox,
                                onToggle = { viewModel.toggleMeal(studentId, data.date, "lunch") }
                            )
                            MealToggleItem(
                                label = "Snacks",
                                subtitle = "Evening Session",
                                isSelected = data.snack,
                                color = SnackColor,
                                enabled = !isMasterLocked && !data.isLeave && !(lockData?.snackLocked ?: false),
                                onToggle = { viewModel.toggleMeal(studentId, data.date, "snack") }
                            )
                            MealToggleItem(
                                label = "Dinner",
                                subtitle = "Night Session",
                                isSelected = data.dinner,
                                color = DinnerColor,
                                enabled = !isMasterLocked && !data.isLeave && !(lockData?.dinnerLocked ?: false),
                                onToggle = { viewModel.toggleMeal(studentId, data.date, "dinner") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Availability Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Single Leave Toggle
                    AvailabilityCard(
                        title = "Mark as Unavailable (On Leave)",
                        subtitle = if (isAnyMainMealLocked) "Status locked: One or more main meals are finalized" else "Locks and sets counts to 0 until toggled OFF",
                        isSelected = data.isLeave,
                        enabled = !isMasterLocked && !isAnyMainMealLocked,
                        onToggle = { viewModel.toggleMeal(studentId, data.date, "leave") },
                        color = MaterialTheme.colorScheme.errorContainer
                    )
                }
            }
            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Unable to load data. Tap to retry.", color = MaterialTheme.colorScheme.error)
                }
            }
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) (color ?: MaterialTheme.colorScheme.secondaryContainer)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        onClick = { if (enabled) onToggle() },
        enabled = enabled
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface 
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
            Switch(
                checked = isSelected,
                onCheckedChange = null,
                enabled = enabled
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
                    modifier = Modifier.size(48.dp),
                    color = color.copy(alpha = if (isSelected) 1f else 0.3f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            label.take(1), 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (enabled) MaterialTheme.colorScheme.onSurface 
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            Switch(
                checked = isSelected,
                onCheckedChange = null,
                enabled = enabled,
                thumbContent = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                } else null
            )
        }
    }
}
