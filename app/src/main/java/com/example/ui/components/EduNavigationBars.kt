package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EduTopAppBar(
    currentUser: UserProfile,
    selectedGrade: Int,
    unreadNotifCount: Int,
    onGradeSelected: (Int) -> Unit,
    onRoleClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Upper Row: App Brand, Role Switcher Button, Notification Bell
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ScholarNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = "EduGrade Logo",
                            tint = AcademicGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "EduGrade",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = ScholarNavy
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = AcademicGold.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "Quality Ed",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AcademicGold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            "CBSE / Secondary Excellence",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Role Switcher Pill button
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onRoleClick() }
                            .testTag("role_switch_button"),
                        color = when (currentUser.role) {
                            UserRole.STUDENT -> ScholarNavy.copy(alpha = 0.12f)
                            UserRole.TEACHER -> ScienceTeal.copy(alpha = 0.15f)
                            UserRole.PRINCIPAL -> AcademicGold.copy(alpha = 0.15f)
                        },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (currentUser.role) {
                                    UserRole.STUDENT -> Icons.Default.Person
                                    UserRole.TEACHER -> Icons.Default.CoPresent
                                    UserRole.PRINCIPAL -> Icons.Default.AccountBalance
                                },
                                contentDescription = "Role",
                                modifier = Modifier.size(16.dp),
                                tint = when (currentUser.role) {
                                    UserRole.STUDENT -> ScholarNavy
                                    UserRole.TEACHER -> ScienceTeal
                                    UserRole.PRINCIPAL -> AcademicGold
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                currentUser.role.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = when (currentUser.role) {
                                    UserRole.STUDENT -> ScholarNavy
                                    UserRole.TEACHER -> ScienceTeal
                                    UserRole.PRINCIPAL -> AcademicGold
                                }
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Switch",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Real-time Notification Bell with Badge
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier.testTag("notification_bell_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifCount > 0) {
                                    Badge(
                                        containerColor = AlertRed,
                                        contentColor = Color.White
                                    ) {
                                        Text("$unreadNotifCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Grade Selector Row (Class 8th, Class 9th, Class 10th Board Focus)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Standard:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                GradeChip(
                    label = "Class 10th (Board)",
                    grade = 10,
                    isSelected = selectedGrade == 10,
                    isBoard = true,
                    onClick = { onGradeSelected(10) }
                )

                GradeChip(
                    label = "Class 9th",
                    grade = 9,
                    isSelected = selectedGrade == 9,
                    isBoard = false,
                    onClick = { onGradeSelected(9) }
                )

                GradeChip(
                    label = "Class 8th",
                    grade = 8,
                    isSelected = selectedGrade == 8,
                    isBoard = false,
                    onClick = { onGradeSelected(8) }
                )
            }
        }
    }
}

@Composable
private fun GradeChip(
    label: String,
    grade: Int,
    isSelected: Boolean,
    isBoard: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("grade_chip_$grade"),
        color = when {
            isSelected && isBoard -> ScholarNavy
            isSelected -> ScienceTeal
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isBoard) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Board Exam",
                    tint = if (isSelected) AcademicGold else AcademicGold.copy(alpha = 0.8f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EduBottomNavigationBar(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        val tabs = listOf(
            Triple(MainTab.DASHBOARD, Icons.Default.Dashboard, "Dashboard"),
            Triple(MainTab.LESSONS, Icons.Default.MenuBook, "Lessons"),
            Triple(MainTab.MOCK_EXAMS, Icons.Default.Quiz, "Mock Exams"),
            Triple(MainTab.DOCUMENTS, Icons.Default.FolderShared, "Repository"),
            Triple(MainTab.MESSAGES, Icons.Default.Chat, "Messages")
        )

        tabs.forEach { (tab, icon, label) ->
            NavigationBarItem(
                selected = currentTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.testTag("tab_icon_${tab.name.lowercase()}")
                    )
                },
                label = {
                    Text(
                        label,
                        fontSize = 11.sp,
                        fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ScholarNavy,
                    selectedTextColor = ScholarNavy,
                    indicatorColor = ScholarNavy.copy(alpha = 0.15f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
            )
        }
    }
}
