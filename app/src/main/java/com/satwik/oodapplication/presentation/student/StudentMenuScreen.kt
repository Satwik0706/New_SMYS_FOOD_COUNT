package com.satwik.oodapplication.presentation.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.satwik.oodapplication.data.model.MealInfo
import com.satwik.oodapplication.presentation.admin.MenuManagementViewModel
import com.satwik.oodapplication.ui.theme.*
import com.satwik.oodapplication.utils.Resource
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun StudentMenuScreen(
    viewModel: MenuManagementViewModel
) {
    // Daily at 7:40 PM, default view switches to Tomorrow
    val initialDate = remember {
        val now = LocalTime.now()
        if (now.isAfter(LocalTime.of(19, 40))) {
            LocalDate.now().plusDays(1)
        } else {
            LocalDate.now()
        }
    }
    
    var currentDate by remember { mutableStateOf(initialDate) }
    val menuState by viewModel.menuState.collectAsState()

    LaunchedEffect(currentDate) {
        viewModel.loadMenu(currentDate.toString())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            "Mess Menu", 
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Check out what's cooking in the mess",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(20.dp))

        // Day Navigation
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val canGoBack = currentDate.isAfter(LocalDate.now().minusDays(5))
                val canGoForward = currentDate.isBefore(LocalDate.now().plusDays(1))

                IconButton(
                    onClick = { if (canGoBack) currentDate = currentDate.minusDays(1) },
                    enabled = canGoBack
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (currentDate == LocalDate.now()) "Today" else currentDate.format(DateTimeFormatter.ofPattern("EEEE")),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = currentDate.format(DateTimeFormatter.ofPattern("MMM dd")),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                IconButton(
                    onClick = { if (canGoForward) currentDate = currentDate.plusDays(1) },
                    enabled = canGoForward
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        when (val state = menuState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(strokeWidth = 3.dp)
                }
            }
            is Resource.Success -> {
                val menu = state.data!!
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item { MenuMealCard("Breakfast", menu.breakfast, BreakfastColor, Icons.Default.BakeryDining) }
                    item { MenuMealCard("Lunch", menu.lunch, LunchColor, Icons.Default.Restaurant) }
                    item { MenuMealCard("Snacks", menu.snack, SnackColor, Icons.Default.Fastfood) }
                    item { MenuMealCard("Dinner", menu.dinner, DinnerColor, Icons.Default.DinnerDining) }
                }
            }
            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No menu found for this date.")
                }
            }
        }
    }
}

@Composable
fun MenuMealCard(name: String, info: MealInfo, accentColor: Color, icon: ImageVector) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column {
            // Header area with accent color
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(accentColor.copy(alpha = 0.15f))
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = accentColor,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, null, modifier = Modifier.size(20.dp), tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        name, 
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            info.timing,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor
                        )
                    }
                }
            }
            
            // Items area
            Column(modifier = Modifier.padding(20.dp)) {
                if (info.items.isEmpty() || (info.items.size == 1 && info.items[0].isBlank())) {
                    Text(
                        "No items listed for this meal.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    info.items.forEach { item ->
                        if (item.isNotBlank()) {
                            Row(
                                modifier = Modifier.padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(6.dp),
                                    shape = CircleShape,
                                    color = accentColor
                                ) {}
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    item.trim(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 17.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
