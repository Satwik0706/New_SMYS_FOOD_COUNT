package com.satwik.oodapplication.presentation.admin

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.satwik.oodapplication.data.model.User
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
    val conflicts by viewModel.conflicts.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var selectedBatch by remember { mutableStateOf("All") }
    var filterNotOnLeave by remember { mutableStateOf(false) }
    var filterDinnerOrdered by remember { mutableStateOf(false) }

    val batches = listOf("All") + students.mapNotNull { it.year }.distinct().sorted()
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        if (uiState is Resource.Success) {
            Toast.makeText(context, "Attendance saved successfully!", Toast.LENGTH_SHORT).show()
        } else if (uiState is Resource.Error) {
            Toast.makeText(context, (uiState as Resource.Error).message ?: "Failed to save attendance", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Daily Attendance", 
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = Color(0xFFFFD700)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            // Batch Filter Tabs
            ScrollableTabRow(
                selectedTabIndex = batches.indexOf(selectedBatch).coerceAtLeast(0),
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                batches.forEach { batch ->
                    Tab(
                        selected = selectedBatch == batch,
                        onClick = { selectedBatch = batch },
                        text = { 
                            Text(
                                batch, 
                                fontWeight = FontWeight.ExtraBold,
                                color = if (selectedBatch == batch) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            ) 
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterNotOnLeave,
                    onClick = { filterNotOnLeave = !filterNotOnLeave },
                    label = { Text("Not on Leave", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                    leadingIcon = if (filterNotOnLeave) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFFFD700)) }
                    } else null,
                    border = BorderStroke(1.dp, if (filterNotOnLeave) Color(0xFFFFD700) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )
                FilterChip(
                    selected = filterDinnerOrdered,
                    onClick = { filterDinnerOrdered = !filterDinnerOrdered },
                    label = { Text("Dinner Ordered", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                    leadingIcon = if (filterDinnerOrdered) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFFFD700)) }
                    } else null,
                    border = BorderStroke(1.dp, if (filterDinnerOrdered) Color(0xFFFFD700) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Group students by year and sort alphabetically by name (A -> Z) inside each year group
            val groupedStudents = remember(students, selectedBatch, filterNotOnLeave, filterDinnerOrdered, foodCounts) {
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
                            !student.isLeave && student.dinnerPref
                        }
                    }
                }

                filtered.groupBy { it.year ?: "Other" }
                    .toSortedMap(compareBy { yearName -> yearName })
                    .mapValues { (_, studentList) ->
                        studentList.sortedBy { it.name.lowercase() }
                    }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                groupedStudents.forEach { (year, studentList) ->
                    item(key = "header_$year") {
                        YearGroupHeader(yearName = year, count = studentList.size)
                    }
                    items(
                        items = studentList,
                        key = { student -> "${student.uid}_${attendanceMap[student.uid] ?: Constants.ATTENDANCE_PRESENT}" }
                    ) { student ->
                        val status = attendanceMap[student.uid] ?: Constants.ATTENDANCE_PRESENT
                        val count = foodCounts[student.uid]
                        val isDinnerOrdered = if (count != null) (!count.isOnLeave && count.isDinner) else (!student.isLeave && student.dinnerPref)
                        val isOnLeave = count?.isOnLeave ?: student.isLeave

                        AttendanceCard(
                            student = student,
                            selectedStatus = status,
                            isDinnerOrdered = isDinnerOrdered,
                            isOnLeave = isOnLeave,
                            onStatusSelected = { newStatus ->
                                viewModel.setAttendanceStatus(student.uid, newStatus)
                            }
                        )
                    }
                }
            }

            Button(
                onClick = { viewModel.submitAttendance() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF6D00),
                    contentColor = Color.White
                )
            ) {
                if (uiState is Resource.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Submit Attendance", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // Food Conflict Report Dialog
    if (conflicts.isNotEmpty()) {
        val absentConflicts = conflicts.filter { it.status == Constants.ATTENDANCE_ABSENT }
        val permissionConflicts = conflicts.filter { it.status == Constants.ATTENDANCE_PERMISSION }
        val leaveConflicts = conflicts.filter { it.status == Constants.ATTENDANCE_LEAVE }

        Dialog(
            onDismissRequest = { viewModel.dismissConflicts() },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.82f)
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f)),
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Dialog Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            color = Color(0xFFFF1744).copy(alpha = 0.15f),
                            shape = CircleShape,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFFF1744),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Food Mismanagement Report",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "${conflicts.size} Conflict(s) Detected",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFFF1744),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "The following students have dinner ordered but were marked with non-present attendance:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Conflict Items Scrollable List
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (absentConflicts.isNotEmpty()) {
                            item {
                                ConflictCategoryHeader(
                                    title = "Absent with Dinner Ordered (${absentConflicts.size})",
                                    color = Color(0xFFFF1744)
                                )
                            }
                            items(absentConflicts) { conflict ->
                                ConflictStudentRow(
                                    student = conflict.student,
                                    statusLabel = "Absent",
                                    color = Color(0xFFFF1744)
                                )
                            }
                        }

                        if (permissionConflicts.isNotEmpty()) {
                            item {
                                ConflictCategoryHeader(
                                    title = "Permission with Dinner Ordered (${permissionConflicts.size})",
                                    color = Color(0xFF00E5FF)
                                )
                            }
                            items(permissionConflicts) { conflict ->
                                ConflictStudentRow(
                                    student = conflict.student,
                                    statusLabel = "Permission",
                                    color = Color(0xFF00E5FF)
                                )
                            }
                        }

                        if (leaveConflicts.isNotEmpty()) {
                            item {
                                ConflictCategoryHeader(
                                    title = "Leave with Dinner Ordered (${leaveConflicts.size})",
                                    color = Color(0xFFFFD700)
                                )
                            }
                            items(leaveConflicts) { conflict ->
                                ConflictStudentRow(
                                    student = conflict.student,
                                    statusLabel = "Leave",
                                    color = Color(0xFFFFD700)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Buttons
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.revertConflictsToPresent() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF6D00),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Acknowledge & Revert to Present", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.confirmAndSaveWithConflicts() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFFF1744))
                            ) {
                                Text("Save As-Is", color = Color(0xFFFF1744), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            TextButton(
                                onClick = { viewModel.dismissConflicts() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {
                                Text("Review & Edit", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun YearGroupHeader(yearName: String, count: Int) {
    Surface(
        color = Color(0xFF262000),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = yearName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFFD700)
            )
            Surface(
                color = Color(0xFFFFD700).copy(alpha = 0.2f),
                shape = CircleShape
            ) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    color = Color(0xFFFFD700)
                )
            }
        }
    }
}

@Composable
fun AttendanceCard(
    student: User,
    selectedStatus: String,
    isDinnerOrdered: Boolean,
    isOnLeave: Boolean,
    onStatusSelected: (String) -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.25f)),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Student Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Metallic Gold Year badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF332B00),
                        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            text = getYearAbbreviation(student.year),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            color = Color(0xFFFFD700)
                        )
                    }

                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (!student.rollNumber.isNullOrBlank()) {
                    Text(
                        text = "Roll: ${student.rollNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                // Indicators: Dinner ordered / Leave status
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    if (isDinnerOrdered) {
                        Surface(
                            color = Color(0xFF003816),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFF00E676))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    Icons.Default.DinnerDining,
                                    contentDescription = null,
                                    tint = Color(0xFF00E676),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    "Dinner",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E676),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    if (isOnLeave) {
                        Surface(
                            color = Color(0xFF3B1E00),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFFFF6D00))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    Icons.Default.EventBusy,
                                    contentDescription = null,
                                    tint = Color(0xFFFF6D00),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    "On Leave",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF6D00),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right Side: Compact horizontal options (Present, Absent, Permission, Leave)
            AttendanceStatusSelector(
                selectedStatus = selectedStatus,
                onStatusSelected = onStatusSelected
            )
        }
    }
}

@Composable
fun AttendanceStatusSelector(
    selectedStatus: String,
    onStatusSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val statuses = listOf(
        Constants.ATTENDANCE_PRESENT to ("P" to Color(0xFF00E676)),
        Constants.ATTENDANCE_ABSENT to ("A" to Color(0xFFFF1744)),
        Constants.ATTENDANCE_PERMISSION to ("Perm" to Color(0xFF00E5FF)),
        Constants.ATTENDANCE_LEAVE to ("Leave" to Color(0xFFFFD700))
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        statuses.forEach { (status, labelAndColor) ->
            val (label, color) = labelAndColor
            val isSelected = selectedStatus == status

            Surface(
                onClick = { onStatusSelected(status) },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) color.copy(alpha = 0.28f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                modifier = Modifier.height(34.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 7.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (isSelected) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ConflictCategoryHeader(title: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ConflictStudentRow(student: User, statusLabel: String, color: Color) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${student.year ?: "Unknown Year"} • Roll: ${student.rollNumber ?: "N/A"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Surface(
                color = color.copy(alpha = 0.2f),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, color.copy(alpha = 0.6f))
            ) {
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = color,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

private fun getYearAbbreviation(year: String?): String {
    if (year.isNullOrBlank()) return "Y"
    return when {
        year.contains("1", ignoreCase = true) -> "Y1"
        year.contains("2", ignoreCase = true) -> "Y2"
        year.contains("3", ignoreCase = true) -> "Y3"
        year.contains("4", ignoreCase = true) -> "Y4"
        else -> year.take(3).uppercase()
    }
}
