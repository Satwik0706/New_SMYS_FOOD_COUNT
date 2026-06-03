package com.satwik.oodapplication.presentation.admin

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.satwik.oodapplication.data.model.MealInfo
import com.satwik.oodapplication.data.model.Menu
import com.satwik.oodapplication.ui.theme.*
import com.satwik.oodapplication.utils.Resource
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuManagementScreen(
    viewModel: MenuManagementViewModel,
    onBack: () -> Unit,
    showBack: Boolean = true
) {
    var currentDate by remember { mutableStateOf(LocalDate.now()) }
    val dateStr = currentDate.toString()
    val menuState by viewModel.menuState.collectAsState()
    var editingMeal by remember { mutableStateOf<MealData?>(null) }
    
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(currentDate) {
        viewModel.loadMenu(dateStr)
        visible = true
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Menu Editor", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    if (showBack) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
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
            // Day Navigation
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val canGoBack = currentDate.isAfter(LocalDate.now())
                    val canGoForward = currentDate.isBefore(LocalDate.now().plusDays(6))

                    IconButton(
                        onClick = { if (canGoBack) currentDate = currentDate.minusDays(1) },
                        enabled = canGoBack,
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Day")
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (currentDate == LocalDate.now()) "Today" else currentDate.format(DateTimeFormatter.ofPattern("EEEE")),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = currentDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    IconButton(
                        onClick = { if (canGoForward) currentDate = currentDate.plusDays(1) },
                        enabled = canGoForward,
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Day")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Meal Configuration", 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Customize daily items for students", 
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            when (val state = menuState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(strokeWidth = 3.dp)
                    }
                }
                is Resource.Success -> {
                    val menu = state.data!!
                    
                    // Set default timings if not already set
                    val breakfastInfo = menu.breakfast.let { if (it.timing.isBlank()) it.copy(timing = "7:30 am to 11:00 am") else it }
                    val lunchInfo = menu.lunch.let { if (it.timing.isBlank()) it.copy(timing = "12:30 to 2:30 pm") else it }
                    val snackInfo = menu.snack.let { if (it.timing.isBlank()) it.copy(timing = "5:00 pm onwards") else it }
                    val dinnerInfo = menu.dinner.let { if (it.timing.isBlank()) it.copy(timing = "7:40 pm onwards") else it }

                    val meals = listOf(
                        MealData("Breakfast", breakfastInfo, BreakfastColor, Icons.Default.Edit),
                        MealData("Lunch", lunchInfo, LunchColor, Icons.Default.Edit),
                        MealData("Snack", snackInfo, SnackColor, Icons.Default.Edit),
                        MealData("Dinner", dinnerInfo, DinnerColor, Icons.Default.Edit)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(meals) { meal ->
                            AdminMealCard(meal) { editingMeal = meal }
                        }
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("System Error: ${state.message}")
                    }
                }
            }
        }

        editingMeal?.let { meal ->
            EditMealDialog(
                mealName = meal.name,
                currentInfo = meal.info,
                onDismiss = { editingMeal = null },
                onSave = { items, timing ->
                    viewModel.updateMeal(dateStr, meal.name, items, timing)
                    editingMeal = null
                }
            )
        }
    }
}

@Composable
fun AdminMealCard(meal: MealData, onClick: () -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color.Black)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                color = meal.color.copy(alpha = 0.9f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(meal.name.take(1), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(meal.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text(
                meal.info.timing, 
                style = MaterialTheme.typography.labelSmall, 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (meal.info.items.all { it.isBlank() }) "Not set" else meal.info.items.joinToString(", "),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun EditMealDialog(
    mealName: String,
    currentInfo: MealInfo,
    onDismiss: () -> Unit,
    onSave: (List<String>, String) -> Unit
) {
    val defaultTiming = when(mealName.lowercase()) {
        "breakfast" -> "7:30 am to 11:00 am"
        "lunch" -> "12:30 to 2:30 pm"
        "snack" -> "5:00 pm onwards"
        "dinner" -> "7:40 pm onwards"
        else -> ""
    }
    
    var items by remember { mutableStateOf(currentInfo.items.joinToString(", ")) }
    var timing by remember { mutableStateOf(if (currentInfo.timing.isBlank()) defaultTiming else currentInfo.timing) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Configure $mealName", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = timing,
                    onValueChange = { timing = it },
                    label = { Text("Service Timing") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = items,
                    onValueChange = { items = it },
                    label = { Text("Menu Items (comma separated)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val itemsList = items.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    onSave(itemsList, timing) 
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Confirm Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

data class MealData(
    val name: String,
    val info: MealInfo,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
