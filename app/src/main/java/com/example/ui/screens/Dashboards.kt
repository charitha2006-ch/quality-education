package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.*
import com.example.data.repository.EduRepository
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StudentDashboardScreen(
    user: UserProfile,
    selectedGrade: Int,
    examAttempts: List<ExamAttemptEntity>,
    notifications: List<NotificationEntity>,
    onStartExam: (MockExam) -> Unit,
    onOpenLesson: (Lesson) -> Unit,
    onNavigateTab: (com.example.ui.viewmodel.MainTab) -> Unit
) {
    val gradeExams = remember(selectedGrade) {
        EduRepository.MOCK_EXAMS.filter { it.grade == selectedGrade }
    }
    val gradeLessons = remember(selectedGrade) {
        EduRepository.INTERACTIVE_LESSONS.filter { it.grade == selectedGrade }
    }

    val studentAttempts = examAttempts.filter { it.grade == selectedGrade }
    val averageScore = if (studentAttempts.isNotEmpty()) {
        studentAttempts.map { it.accuracyPercent }.average().toInt()
    } else 88

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("student_dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Welcome & Board Focus Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("board_countdown_card"),
                colors = CardDefaults.cardColors(containerColor = ScholarNavy),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = AcademicGold.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Star, contentDescription = "Board", tint = AcademicGold, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (selectedGrade == 10) "CBSE 10th Board Exam Target" else "Class ${selectedGrade}th Foundation Target",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AcademicGold
                                )
                            }
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Target: 95%+",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        "Welcome back, ${user.name}!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        if (selectedGrade == 10)
                            "Class 10 Board exam syllabus is 76% mastered. Focus on Light Ray Optics and Quadratic factorizations today."
                        else
                            "Class ${selectedGrade}th concepts foundation is progressing steadily. Complete your chapter simulations.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress bar
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Syllabus Mastery", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                            Text("$averageScore%", color = AcademicGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { averageScore / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = AcademicGold,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }

        // Key Performance KPI metrics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KPISmallCard(
                    title = "Avg Score",
                    value = "$averageScore%",
                    icon = Icons.Default.Grade,
                    accentColor = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
                KPISmallCard(
                    title = "Mock Tests",
                    value = "${studentAttempts.size + 2}",
                    icon = Icons.Default.AssignmentTurnedIn,
                    accentColor = ScholarNavy,
                    modifier = Modifier.weight(1f)
                )
                KPISmallCard(
                    title = "Board Rank",
                    value = "#3 in 10A",
                    icon = Icons.Default.EmojiEvents,
                    accentColor = AcademicGold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Real-time Notification Banner
        val latestNotif = notifications.firstOrNull()
        if (latestNotif != null) {
            item {
                Surface(
                    color = AcademicGold.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = "Notification",
                            tint = AcademicGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                latestNotif.title,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = ScholarNavy
                            )
                            Text(
                                latestNotif.message,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Interactive Lessons Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Interactive Lessons & Simulators",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ScholarNavy
                )
                TextButton(onClick = { onNavigateTab(com.example.ui.viewmodel.MainTab.LESSONS) }) {
                    Text("View All", fontSize = 12.sp)
                }
            }
        }

        items(gradeLessons) { lesson ->
            LessonItemCard(lesson = lesson, onClick = { onOpenLesson(lesson) })
        }

        // Mock Exams Section
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (selectedGrade == 10) "Class 10 Board Mock Exams" else "Class ${selectedGrade}th Practice Tests",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ScholarNavy
                )
                TextButton(onClick = { onNavigateTab(com.example.ui.viewmodel.MainTab.MOCK_EXAMS) }) {
                    Text("More Tests", fontSize = 12.sp)
                }
            }
        }

        items(gradeExams) { exam ->
            MockExamCard(exam = exam, onStart = { onStartExam(exam) })
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun TeacherDashboardScreen(
    user: UserProfile,
    selectedGrade: Int,
    examAttempts: List<ExamAttemptEntity>,
    onSendAlert: () -> Unit,
    onUploadMaterial: () -> Unit,
    onStartExam: (MockExam) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("teacher_dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Teacher Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ScienceTeal),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "Teacher & Mentor Portal",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            "Class ${selectedGrade}th Analytics",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "${user.name} (Science HOD)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        "Monitoring 128 students across 3 sections for Class ${selectedGrade}th. Class 10 Board preparation index is at 89.2%.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onSendAlert,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = ScienceTeal),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("teacher_send_alert_button")
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = "Alert", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Send Class Alert", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onUploadMaterial,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = "Upload", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share Notes", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // At-Risk Intervention Alert Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AlertRed.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AlertRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.WarningAmber, contentDescription = "Intervention", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Performance Intervention Needed",
                            fontWeight = FontWeight.Bold,
                            color = AlertRed,
                            fontSize = 13.sp
                        )
                        Text(
                            "3 students scored <60% in Quadratic Equations Mock. Remedial doubt session recommended.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Class Performance Overview Table
        item {
            Text(
                "Student Progress & Board Readiness (Class 10A)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ScholarNavy
            )
        }

        val studentRoster = listOf(
            Triple("Aarav Sharma", 90, "Distinction - Ready"),
            Triple("Priya Patel", 95, "Top Ranker - Outstanding"),
            Triple("Rohan Gupta", 55, "Needs Remedial Support"),
            Triple("Ananya Sen", 84, "Good - Consistent"),
            Triple("Vikram Malhotra", 78, "Average - Optics Weak")
        )

        items(studentRoster) { (name, score, status) ->
            StudentRosterCard(name = name, score = score, status = status)
        }

        // Topic Mastery Metrics
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Class 10th Topic Mastery Benchmarks",
                        fontWeight = FontWeight.Bold,
                        color = ScholarNavy,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TopicProgressBar("Optics & Ray Reflection", 92, SuccessGreen)
                    TopicProgressBar("Chemical Reactions & Equations", 84, ScienceTeal)
                    TopicProgressBar("Electricity & Circuit Laws", 78, AcademicGold)
                    TopicProgressBar("Quadratic Equations Factorization", 68, AlertRed)
                }
            }
        }
    }
}

@Composable
fun PrincipalDashboardScreen(
    user: UserProfile,
    onBroadcastCircular: () -> Unit,
    onNavigateTab: (com.example.ui.viewmodel.MainTab) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("principal_dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Principal Institutional Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ScholarNavyDark),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = AcademicGold.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "Academic Governance",
                                color = AcademicGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            "DPS Senior Wing",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Dr. K. R. Ramanathan",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        "Institutional Quality Education Index: 87.8%. Class 10 Board Readiness is at 89.2% with 94.6% student engagement.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onBroadcastCircular,
                        colors = ButtonDefaults.buttonColors(containerColor = AcademicGold, contentColor = ScholarNavyDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("principal_broadcast_button")
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = "Broadcast", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Broadcast School-wide Circular", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Institutional KPIs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KPISmallCard("Class 10 Ready", "89.2%", Icons.Default.Verified, SuccessGreen, Modifier.weight(1f))
                KPISmallCard("Active Students", "640", Icons.Default.Groups, ScholarNavy, Modifier.weight(1f))
                KPISmallCard("Mock Tests Taken", "1,280", Icons.Default.Quiz, AcademicGold, Modifier.weight(1f))
            }
        }

        // Grade Comparative Analysis Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Grade-wise Quality Education Benchmarks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ScholarNavy
                    )
                    Text(
                        "Comparative assessment performance across Classes 8th, 9th, and 10th",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    GradeBenchmarkBar("Class 10th (Board Prep Focus)", 89, ScholarNavy, "Target: 95% (Board Exams)")
                    GradeBenchmarkBar("Class 9th (Secondary Foundation)", 81, ScienceTeal, "Target: 85% (Annual Exams)")
                    GradeBenchmarkBar("Class 8th (Middle School Bridge)", 84, AcademicGold, "Target: 85% (Annual Exams)")
                }
            }
        }

        // Document Repository & Resource Governance
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Cloud Learning Repository Audit",
                            fontWeight = FontWeight.Bold,
                            color = ScholarNavy,
                            fontSize = 14.sp
                        )
                        TextButton(onClick = { onNavigateTab(com.example.ui.viewmodel.MainTab.DOCUMENTS) }) {
                            Text("Open Repository", fontSize = 12.sp)
                        }
                    }
                    Text(
                        "142 documents active. 10-Year CBSE Past Solved Papers downloaded 184 times. 100% curriculum compliance checked.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun KPISmallCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StudentRosterCard(name: String, score: Int, status: String) {
    val isRisk = score < 60
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isRisk) AlertRed.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isRisk) AlertRed.copy(alpha = 0.15f) else ScholarNavy.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${name.first()}",
                        fontWeight = FontWeight.Bold,
                        color = if (isRisk) AlertRed else ScholarNavy,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(status, fontSize = 11.sp, color = if (isRisk) AlertRed else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Surface(
                color = if (isRisk) AlertRed.copy(alpha = 0.15f) else SuccessGreen.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    "$score%",
                    fontWeight = FontWeight.Bold,
                    color = if (isRisk) AlertRed else SuccessGreen,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun TopicProgressBar(topic: String, percent: Int, color: Color) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(topic, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text("$percent%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun GradeBenchmarkBar(label: String, score: Int, color: Color, targetSub: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text("$score%", fontWeight = FontWeight.ExtraBold, color = color, fontSize = 13.sp)
        }
        Text(targetSub, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun LessonItemCard(lesson: Lesson, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("lesson_card_${lesson.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when (lesson.subject) {
                            SubjectType.SCIENCE -> ScienceTeal.copy(alpha = 0.15f)
                            SubjectType.MATHEMATICS -> ScholarNavy.copy(alpha = 0.15f)
                            else -> AcademicGold.copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (lesson.subject) {
                        SubjectType.SCIENCE -> Icons.Default.Science
                        SubjectType.MATHEMATICS -> Icons.Default.Functions
                        SubjectType.SOCIAL_SCIENCE -> Icons.Default.Public
                        SubjectType.ENGLISH -> Icons.Default.MenuBook
                    },
                    contentDescription = lesson.subject.displayName,
                    tint = when (lesson.subject) {
                        SubjectType.SCIENCE -> ScienceTeal
                        SubjectType.MATHEMATICS -> ScholarNavy
                        else -> AcademicGold
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        lesson.subject.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ScholarNavy
                    )
                    if (lesson.interactiveToolType != InteractiveToolType.NONE) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = AcademicGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "⚡ Simulator",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = AcademicGold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    lesson.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    lesson.chapter,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MockExamCard(exam: MockExam, onStart: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("exam_card_${exam.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (exam.isBoardExam) {
                        Surface(
                            color = AcademicGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "BOARD SIMULATOR",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AcademicGold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        exam.subject.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ScholarNavy
                    )
                }

                Text(
                    "${exam.durationMinutes} mins • ${exam.totalMarks} Marks",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                exam.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                exam.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = ScholarNavy),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("start_exam_${exam.id}")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Start Mock Exam", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
