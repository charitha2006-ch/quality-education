package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExamAttemptEntity
import com.example.data.model.Lesson
import com.example.data.model.MockExam
import com.example.data.model.SubjectType
import com.example.data.repository.EduRepository
import com.example.ui.theme.AcademicGold
import com.example.ui.theme.ScholarNavy
import com.example.ui.theme.ScienceTeal

@Composable
fun LessonsListScreen(
    selectedGrade: Int,
    onOpenLesson: (Lesson) -> Unit
) {
    var selectedSubject by remember { mutableStateOf("All") }

    val allLessons = remember(selectedGrade) {
        EduRepository.INTERACTIVE_LESSONS.filter { it.grade == selectedGrade }
    }

    val filteredLessons = remember(allLessons, selectedSubject) {
        if (selectedSubject == "All") allLessons
        else allLessons.filter { it.subject.displayName.equals(selectedSubject, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("lessons_list_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Interactive Lessons & Simulators",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ScholarNavy
                )
                Text(
                    "Class ${selectedGrade}th Interactive Concepts with Embedded Simulation",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                color = AcademicGold.copy(alpha = 0.18f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "${filteredLessons.size} chapters",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AcademicGold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Subject Filter
        val subjects = listOf("All", "Science", "Mathematics", "Social Science")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(subjects) { subj ->
                val isSelected = selectedSubject.equals(subj, ignoreCase = true)
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedSubject = subj },
                    label = { Text(subj, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ScholarNavy,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("lesson_filter_$subj")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredLessons, key = { it.id }) { lesson ->
                LessonItemCard(lesson = lesson, onClick = { onOpenLesson(lesson) })
            }
        }
    }
}

@Composable
fun MockExamsScreen(
    selectedGrade: Int,
    examAttempts: List<ExamAttemptEntity>,
    onStartExam: (MockExam) -> Unit
) {
    val gradeExams = remember(selectedGrade) {
        EduRepository.MOCK_EXAMS.filter { it.grade == selectedGrade }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("mock_exams_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ScholarNavy),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        if (selectedGrade == 10) "Class 10 CBSE Board Mock Examination Series" else "Class ${selectedGrade}th Standardized Assessment Series",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Timed practice simulating exact board examination marking schemes, negative marking rules, and topic distribution.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Text(
                "Available Mock Test Papers (Class ${selectedGrade}th)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ScholarNavy
            )
        }

        items(gradeExams) { exam ->
            MockExamCard(exam = exam, onStart = { onStartExam(exam) })
        }

        if (examAttempts.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Your Recent Exam Attempts & Performance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ScholarNavy
                )
            }

            items(examAttempts) { attempt ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(attempt.examTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                "Class ${attempt.grade}th • Score: ${attempt.score}/${attempt.totalMarks}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = if (attempt.accuracyPercent >= 80) ScienceTeal.copy(alpha = 0.15f) else AcademicGold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "${attempt.accuracyPercent}% Accuracy",
                                fontWeight = FontWeight.Bold,
                                color = if (attempt.accuracyPercent >= 80) ScienceTeal else AcademicGold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
