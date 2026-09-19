package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.InteractiveToolType
import com.example.data.model.Lesson
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveLessonView(
    lesson: Lesson,
    onClose: () -> Unit
) {
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var hasAnswered by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            lesson.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            "Class ${lesson.grade}th • ${lesson.subject.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose, modifier = Modifier.testTag("close_lesson_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Surface(
                        color = ScholarNavy.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = "Duration",
                                tint = ScholarNavy,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${lesson.readingTimeMinutes} min read",
                                fontSize = 11.sp,
                                color = ScholarNavy,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Chapter Tag
            item {
                Surface(
                    color = AcademicGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        lesson.chapter,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AcademicGold
                    )
                }
            }

            // Overview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = "Concept",
                                tint = ScholarNavy,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Core Conceptual Overview",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ScholarNavy
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            lesson.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Interactive Simulator Component
            item {
                when (lesson.interactiveToolType) {
                    InteractiveToolType.OHMS_LAW_SIMULATOR -> {
                        OhmsLawSimulator()
                    }
                    InteractiveToolType.QUADRATIC_SOLVER -> {
                        QuadraticFormulaSolver()
                    }
                    InteractiveToolType.LENS_REFRACTION_SIM -> {
                        LensRefractionSimulator()
                    }
                    else -> {}
                }
            }

            // High Yield Takeaways
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = "Takeaways",
                                tint = ScienceTeal,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "High-Yield Board Takeaways",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ScienceTeal
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        lesson.keyTakeaways.forEach { takeaway ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Bullet",
                                    tint = SuccessGreen,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    takeaway,
                                    style = MaterialTheme.typography.bodySmall,
                                    lineHeight = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Check Your Understanding Quiz Widget
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("practice_quiz_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Check Your Understanding",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ScholarNavy
                            )
                            Surface(
                                color = ScholarNavy.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "Board Check",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ScholarNavy,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            lesson.practiceQuestion,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        lesson.practiceOptions.forEachIndexed { idx, opt ->
                            val isChosen = selectedOption == idx
                            val isCorrect = idx == lesson.correctOptionIndex

                            val borderCol = when {
                                hasAnswered && isCorrect -> SuccessGreen
                                hasAnswered && isChosen && !isCorrect -> AlertRed
                                isChosen -> ScholarNavy
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            }

                            val bgCol = when {
                                hasAnswered && isCorrect -> SuccessGreen.copy(alpha = 0.12f)
                                hasAnswered && isChosen && !isCorrect -> AlertRed.copy(alpha = 0.12f)
                                isChosen -> ScholarNavy.copy(alpha = 0.08f)
                                else -> MaterialTheme.colorScheme.surface
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        if (!hasAnswered) {
                                            selectedOption = idx
                                            hasAnswered = true
                                        }
                                    }
                                    .border(1.5.dp, borderCol, RoundedCornerShape(10.dp))
                                    .testTag("practice_opt_$idx"),
                                color = bgCol
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .defaultMinSize(minHeight = 48.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (hasAnswered && isCorrect) SuccessGreen
                                                else if (hasAnswered && isChosen) AlertRed
                                                else if (isChosen) ScholarNavy
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "${'A' + idx}",
                                            color = if (isChosen || (hasAnswered && isCorrect)) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        opt,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        if (hasAnswered) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = if (selectedOption == lesson.correctOptionIndex) SuccessGreen.copy(alpha = 0.12f) else AlertRed.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        if (selectedOption == lesson.correctOptionIndex) "Correct! Well Done." else "Not quite right.",
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedOption == lesson.correctOptionIndex) SuccessGreen else AlertRed,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        lesson.explanation,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
