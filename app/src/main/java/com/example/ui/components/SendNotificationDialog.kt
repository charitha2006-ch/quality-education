package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.ui.theme.ScholarNavy

@Composable
fun SendNotificationDialog(
    currentUser: UserProfile,
    initialGrade: Int,
    onDismiss: () -> Unit,
    onSendAlert: (title: String, message: String, grade: Int) -> Unit,
    onBroadcastCircular: (title: String, message: String) -> Unit
) {
    val isPrincipal = currentUser.role == UserRole.PRINCIPAL
    var title by remember {
        mutableStateOf(
            if (isPrincipal) "Annual Board Examination Guidelines 2026"
            else "Science Doubt Clearance & Mock Review Session"
        )
    }
    var message by remember {
        mutableStateOf(
            if (isPrincipal) "Pre-board examination timetable and admit card distribution will begin next Monday. Strict 100% attendance is mandatory."
            else "Students who scored below 70% in Optics & Electricity are requested to join the remedial clinic tomorrow at 3 PM in Room 204."
        )
    }
    var targetGrade by remember { mutableIntStateOf(initialGrade) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Campaign, contentDescription = "Broadcast", tint = ScholarNavy)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isPrincipal) "Broadcast School Circular" else "Send Class Notification",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Notification Title") },
                    modifier = Modifier.fillMaxWidth().testTag("notif_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Announcement Content") },
                    modifier = Modifier.fillMaxWidth().testTag("notif_msg_input"),
                    maxLines = 4
                )

                if (!isPrincipal) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Target Class: ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        listOf(8, 9, 10).forEach { g ->
                            FilterChip(
                                selected = targetGrade == g,
                                onClick = { targetGrade = g },
                                label = { Text("Class $g") },
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        "Recipients: All Students & Faculty (Institutional Quality Broadcast)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && message.isNotBlank()) {
                        if (isPrincipal) {
                            onBroadcastCircular(title.trim(), message.trim())
                        } else {
                            onSendAlert(title.trim(), message.trim(), targetGrade)
                        }
                    }
                },
                enabled = title.isNotBlank() && message.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ScholarNavy),
                modifier = Modifier.testTag("submit_broadcast_button")
            ) {
                Text(if (isPrincipal) "Broadcast to School" else "Send to Class")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
