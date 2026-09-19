package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.data.model.ExamAttemptEntity
import com.example.data.model.MockExam
import com.example.ui.theme.*
import com.example.ui.viewmodel.ExamSessionState

@Composable
fun MockExamSessionView(
    session: ExamSessionState,
    onAnswerSelected: (questionId: Int, optionIndex: Int) -> Unit,
    onToggleReview: (questionId: Int) -> Unit,
    onNavigate: (index: Int) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    var showSubmitConfirm by remember { mutableStateOf(false) }

    val exam = session.exam
    val questions = exam.questions
    val currentIndex = session.currentQuestionIndex.coerceIn(0, questions.size - 1)
    val currentQuestion = questions[currentIndex]

    val minutes = session.timeRemainingSeconds / 60
    val seconds = session.timeRemainingSeconds % 60
    val timerString = "%02d:%02d".format(minutes, seconds)
    val isLowTime = session.timeRemainingSeconds < 120

    val answeredCount = session.selectedAnswers.size
    val reviewCount = session.markedForReview.size

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onCancel) {
                            Icon(Icons.Default.Close, contentDescription = "Exit Exam")
                        }

                        // Live Countdown Timer Pill
                        Surface(
                            color = if (isLowTime) AlertRed.copy(alpha = 0.15f) else ScholarNavy.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("exam_timer_display")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = "Timer",
                                    tint = if (isLowTime) AlertRed else ScholarNavy,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    timerString,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isLowTime) AlertRed else ScholarNavy,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        Button(
                            onClick = { showSubmitConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ScholarNavy),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("submit_exam_button")
                        ) {
                            Text("Submit", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        exam.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ScholarNavy
                    )
                    Text(
                        "Class ${exam.grade}th • Total Marks: ${exam.totalMarks} • Answered: $answeredCount/${questions.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Question Palette Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(questions) { idx, q ->
                            val isCurrent = idx == currentIndex
                            val isAnswered = session.selectedAnswers.containsKey(q.id)
                            val isMarked = session.markedForReview.contains(q.id)

                            val bgColor = when {
                                isCurrent -> ScholarNavy
                                isMarked -> AcademicGold
                                isAnswered -> SuccessGreen
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                            val textColor = if (isCurrent || isMarked || isAnswered) Color.White else MaterialTheme.colorScheme.onSurface

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(bgColor)
                                    .clickable { onNavigate(idx) }
                                    .testTag("palette_q_${idx + 1}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${idx + 1}",
                                    color = textColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { if (currentIndex > 0) onNavigate(currentIndex - 1) },
                        enabled = currentIndex > 0,
                        modifier = Modifier.testTag("prev_question_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Previous")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Previous")
                    }

                    OutlinedButton(
                        onClick = { onToggleReview(currentQuestion.id) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (session.markedForReview.contains(currentQuestion.id)) AcademicGold else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.testTag("mark_review_button")
                    ) {
                        Icon(
                            if (session.markedForReview.contains(currentQuestion.id)) Icons.Default.BookmarkAdded else Icons.Default.BookmarkBorder,
                            contentDescription = "Review",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (session.markedForReview.contains(currentQuestion.id)) "Marked" else "Review")
                    }

                    Button(
                        onClick = {
                            if (currentIndex < questions.size - 1) {
                                onNavigate(currentIndex + 1)
                            } else {
                                showSubmitConfirm = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ScholarNavy),
                        modifier = Modifier.testTag("next_question_button")
                    ) {
                        Text(if (currentIndex < questions.size - 1) "Next" else "Finish")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = ScholarNavy.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    "Question ${currentIndex + 1} of ${questions.size}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ScholarNavy
                                )
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    currentQuestion.topic,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            currentQuestion.question,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 22.sp,
                            modifier = Modifier.testTag("question_text")
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Options
                        currentQuestion.options.forEachIndexed { optIndex, optionText ->
                            val isSelected = session.selectedAnswers[currentQuestion.id] == optIndex
                            val optionLetter = ('A' + optIndex)

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onAnswerSelected(currentQuestion.id, optIndex) }
                                    .then(
                                        if (isSelected) Modifier.border(2.dp, ScholarNavy, RoundedCornerShape(12.dp))
                                        else Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    )
                                    .testTag("option_${currentQuestion.id}_$optIndex"),
                                color = if (isSelected) ScholarNavy.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(14.dp)
                                        .defaultMinSize(minHeight = 48.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) ScholarNavy else MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "$optionLetter",
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        optionText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSubmitConfirm) {
        AlertDialog(
            onDismissRequest = { showSubmitConfirm = false },
            title = { Text("Submit Mock Exam?", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("You have answered $answeredCount of ${questions.size} questions.")
                    if (reviewCount > 0) {
                        Text(
                            "$reviewCount question(s) are marked for review.",
                            color = AcademicGold,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Are you ready to submit and calculate your board readiness score?")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitConfirm = false
                        onSubmit()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ScholarNavy),
                    modifier = Modifier.testTag("confirm_submit_button")
                ) {
                    Text("Submit Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitConfirm = false }) {
                    Text("Resume Test")
                }
            }
        )
    }
}

@Composable
fun ExamResultDialog(
    attempt: ExamAttemptEntity,
    exam: MockExam?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = "Result",
                    tint = AcademicGold,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Exam Scorecard & Analytics", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // Summary Banner Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ScholarNavyDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "${attempt.score} / ${attempt.totalMarks}",
                                color = AcademicGold,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 32.sp
                            )
                            Text(
                                "${attempt.accuracyPercent}% Accuracy",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            val verdict = when {
                                attempt.accuracyPercent >= 90 -> "🌟 Distinction - Outstanding Board Preparedness!"
                                attempt.accuracyPercent >= 75 -> "✅ Very Good - Minor revisions recommended"
                                attempt.accuracyPercent >= 50 -> "⚠️ Passing - Remedial focus needed in weak concepts"
                                else -> "❌ Needs Attention - Detailed review required"
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                verdict,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Time Spent", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${attempt.timeTakenSeconds / 60}m ${attempt.timeTakenSeconds % 60}s", fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Class Standard", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Class ${attempt.grade}th", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (exam != null) {
                    item {
                        Text(
                            "Question Solutions & Explanations:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ScholarNavy
                        )
                    }
                    itemsIndexed(exam.questions) { idx, q ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "Q${idx + 1}: ${q.question}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Correct Answer: ${q.options.getOrNull(q.correctAnswer) ?: ""}",
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    q.explanation,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ScholarNavy),
                modifier = Modifier.testTag("dismiss_result_button")
            ) {
                Text("Return to Dashboard")
            }
        }
    )
}
