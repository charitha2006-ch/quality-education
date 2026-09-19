package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.EduRepository
import com.example.ui.theme.*

@Composable
fun RoleSwitchDialog(
    currentUser: UserProfile,
    onRoleSelect: (UserRole) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.ManageAccounts,
                    contentDescription = "Switch Login Role",
                    tint = ScholarNavy,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Switch Institutional Role",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Select a personalized portal login to experience role-specific dashboards, analytics, and permissions:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                RoleCard(
                    role = UserRole.STUDENT,
                    name = "Aarav Sharma",
                    designation = "Student - Class 10A (Board Candidate)",
                    description = "Personalized syllabus progress, interactive lessons with simulators, board mock exams & scorecards.",
                    icon = Icons.Default.School,
                    accentColor = ScholarNavy,
                    isSelected = currentUser.role == UserRole.STUDENT,
                    onClick = { onRoleSelect(UserRole.STUDENT) }
                )

                RoleCard(
                    role = UserRole.TEACHER,
                    name = "Dr. Sunita Verma",
                    designation = "Teacher - Science HOD & Board Mentor",
                    description = "Class 8, 9 & 10 performance analytics, at-risk student intervention, publish study notes, send alerts.",
                    icon = Icons.Default.CoPresent,
                    accentColor = ScienceTeal,
                    isSelected = currentUser.role == UserRole.TEACHER,
                    onClick = { onRoleSelect(UserRole.TEACHER) }
                )

                RoleCard(
                    role = UserRole.PRINCIPAL,
                    name = "Dr. K. R. Ramanathan",
                    designation = "Principal - Academic Governance",
                    description = "Institutional quality education KPI, Class 10 board readiness index (89.2%), school circulars broadcast.",
                    icon = Icons.Default.AccountBalance,
                    accentColor = AcademicGold,
                    isSelected = currentUser.role == UserRole.PRINCIPAL,
                    onClick = { onRoleSelect(UserRole.PRINCIPAL) }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_role_dialog")
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun RoleCard(
    role: UserRole,
    name: String,
    designation: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .then(
                if (isSelected) Modifier.border(2.dp, accentColor, RoundedCornerShape(12.dp))
                else Modifier
            )
            .testTag("role_card_${role.name.lowercase()}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accentColor.copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = if (isSelected) accentColor else accentColor.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = role.label,
                        tint = if (isSelected) Color.White else accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isSelected) {
                        Surface(
                            color = accentColor,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "ACTIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    designation,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
