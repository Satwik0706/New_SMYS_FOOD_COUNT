package com.satwik.oodapplication.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.satwik.oodapplication.utils.AppInfo
import kotlinx.coroutines.delay

@Composable
fun AboutInfoDialog(
    onDismiss: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    val isVerified = remember { AppInfo.verifyIntegrity() }
    val isDark = isSystemInDarkTheme()

    // Royal Parchment Letter Palette
    val letterBgColor = if (isDark) Color(0xFF1E1915) else Color(0xFFFFFDF8)
    val cardSurfaceColor = if (isDark) Color(0xFF28211A) else Color(0xFFFFF9EE)
    val goldenAccent = Color(0xFFC59B27)
    val maroonAccent = if (isDark) Color(0xFFFF8A8A) else Color(0xFF800000)
    val textColor = if (isDark) Color(0xFFF7EFE6) else Color(0xFF2A211C)

    LaunchedEffect(Unit) {
        delay(200) // Fast smooth unrolling delay
        isLoading = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .padding(vertical = 12.dp)
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            goldenAccent.copy(alpha = 0.8f),
                            maroonAccent.copy(alpha = 0.4f),
                            goldenAccent.copy(alpha = 0.8f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                ),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = letterBgColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    // Top Bar Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = goldenAccent.copy(alpha = 0.15f),
                                shape = CircleShape,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Spa,
                                        contentDescription = null,
                                        tint = goldenAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "SMYS Community Letter",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = goldenAccent
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = textColor)
                        }
                    }

                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    strokeWidth = 3.dp,
                                    color = goldenAccent,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Unrolling Letter...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textColor.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else if (!isVerified) {
                        // Integrity failure state
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Letter Verification Failed",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "The letter's developer signature or metadata has been altered in source code. Access restricted.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = textColor.copy(alpha = 0.8f)
                            )
                        }
                    } else {
                        // Scrollable Letter Content
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(bottom = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))

                            // Sacred || Shree || Emblem Seal
                            Surface(
                                color = maroonAccent.copy(alpha = 0.12f),
                                shape = CircleShape,
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, goldenAccent.copy(alpha = 0.6f)),
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "॥ श्रीः ॥",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Black,
                                        color = maroonAccent,
                                        textAlign = TextAlign.Center,
                                        letterSpacing = 2.sp
                                    )
                                }
                            }

                            VedicOrnamentDivider(color = goldenAccent)

                            // App Name Title
                            Text(
                                text = AppInfo.APP_NAME,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = textColor,
                                textAlign = TextAlign.Center,
                                letterSpacing = (-0.5).sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Sathpanatha Jubilee Badge
                            Surface(
                                color = goldenAccent.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, goldenAccent.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = goldenAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = AppInfo.SATHPANATHA_TITLE,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = maroonAccent
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Main Letter Parchment Body
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = cardSurfaceColor),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    goldenAccent.copy(alpha = 0.35f)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(18.dp)
                                ) {
                                    // Salutation
                                    Text(
                                        text = "Dear SMYS Hostel Community & Management,",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = maroonAccent
                                    )

                                    // 1. Purpose
                                    VedicParagraph(
                                        heading = "Purpose of the System",
                                        icon = Icons.Default.AutoAwesome,
                                        body = AppInfo.PURPOSE,
                                        textColor = textColor,
                                        accentColor = goldenAccent
                                    )

                                    HorizontalDivider(color = goldenAccent.copy(alpha = 0.2f))

                                    // 2. Acknowledgement
                                    VedicParagraph(
                                        heading = "Acknowledgement",
                                        icon = Icons.Default.VolunteerActivism,
                                        body = AppInfo.ACKNOWLEDGEMENT,
                                        textColor = textColor,
                                        accentColor = goldenAccent
                                    )

                                    HorizontalDivider(color = goldenAccent.copy(alpha = 0.2f))

                                    // 3. Sathpanatha 2K26–2K27 Office Bearers
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Groups,
                                                contentDescription = null,
                                                tint = goldenAccent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                AppInfo.SATHPANATHA_TITLE,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = maroonAccent
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            "The Sathpanatha 2K26–2K27 — 75th Year team comprises the following office bearers:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = textColor.copy(alpha = 0.8f)
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            AppInfo.OFFICE_BEARERS.forEachIndexed { index, bearer ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(
                                                            color = goldenAccent.copy(alpha = 0.08f),
                                                            shape = RoundedCornerShape(10.dp)
                                                        )
                                                        .padding(horizontal = 12.dp, vertical = 7.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        "${index + 1}. ${bearer.title}",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = maroonAccent,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Text(
                                                        bearer.name,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = textColor
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = goldenAccent.copy(alpha = 0.2f))

                                    // 4. Credits & Special Acknowledgements
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = null,
                                                tint = goldenAccent,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "Credits & Special Acknowledgements",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = maroonAccent
                                            )
                                        }

                                        Text(
                                            "• Initiated By: ${AppInfo.INITIATED_BY}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textColor
                                        )

                                        Text(
                                            "• Supported & Suggested By: ${AppInfo.SUPPORTED_BY}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textColor
                                        )

                                        Text(
                                            AppInfo.CREDITS_SUPPORT_TEXT,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = textColor.copy(alpha = 0.8f)
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            "• Testing & Feedback:",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = goldenAccent
                                        )

                                        Text(
                                            AppInfo.TESTING_FEEDBACK,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = textColor.copy(alpha = 0.85f),
                                            lineHeight = 18.sp
                                        )
                                    }

                                    HorizontalDivider(color = goldenAccent.copy(alpha = 0.2f))

                                    // 5. A Letter to the Future
                                    VedicParagraph(
                                        heading = AppInfo.LETTER_TO_FUTURE_TITLE,
                                        icon = Icons.Default.HistoryEdu,
                                        body = AppInfo.LETTER_TO_FUTURE_BODY,
                                        textColor = textColor,
                                        accentColor = goldenAccent
                                    )

                                    VedicOrnamentDivider(color = goldenAccent)

                                    // Valediction & Developer Sign-off
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(
                                            "With Regards,",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = textColor.copy(alpha = 0.8f)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            "Developer",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = goldenAccent
                                        )
                                        Text(
                                            AppInfo.DEVELOPER_NAME,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = maroonAccent
                                        )
                                        Surface(
                                            color = goldenAccent.copy(alpha = 0.18f),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Text(
                                                AppInfo.DEVELOPER_HANDLE,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = textColor
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = maroonAccent,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Close Letter", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VedicOrnamentDivider(
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        HorizontalDivider(
            modifier = Modifier
                .weight(1f)
                .height(1.dp),
            color = color.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        HorizontalDivider(
            modifier = Modifier
                .weight(1f)
                .height(1.dp),
            color = color.copy(alpha = 0.4f)
        )
    }
}

@Composable
fun VedicParagraph(
    heading: String,
    icon: ImageVector,
    body: String,
    textColor: Color,
    accentColor: Color
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                heading,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor.copy(alpha = 0.9f),
            lineHeight = 21.sp
        )
    }
}
