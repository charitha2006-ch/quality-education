package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole(val label: String) {
    STUDENT("Student"),
    TEACHER("Teacher"),
    PRINCIPAL("Principal")
}

data class UserProfile(
    val id: String,
    val name: String,
    val role: UserRole,
    val grade: Int? = null, // e.g. 10 for Student, or null for Principal
    val schoolName: String = "Delhi Public Senior Secondary School",
    val email: String,
    val avatarColorHex: Long = 0xFF1B3B6F,
    val designation: String = ""
)

enum class SubjectType(val displayName: String, val code: String) {
    MATHEMATICS("Mathematics", "MATH"),
    SCIENCE("Science (Phy/Chem/Bio)", "SCI"),
    SOCIAL_SCIENCE("Social Science", "SST"),
    ENGLISH("English Language & Lit", "ENG")
}

enum class InteractiveToolType {
    OHMS_LAW_SIMULATOR,
    QUADRATIC_SOLVER,
    LENS_REFRACTION_SIM,
    CHEMICAL_BALANCER,
    CELL_ORGANELLES,
    NONE
}

data class Lesson(
    val id: String,
    val grade: Int,
    val subject: SubjectType,
    val chapter: String,
    val title: String,
    val summary: String,
    val readingTimeMinutes: Int,
    val keyTakeaways: List<String>,
    val interactiveToolType: InteractiveToolType,
    val practiceQuestion: String,
    val practiceOptions: List<String>,
    val correctOptionIndex: Int,
    val explanation: String,
    val isCompleted: Boolean = false
)

data class ExamQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctAnswer: Int,
    val explanation: String,
    val topic: String,
    val difficulty: String = "Board Standard"
)

data class MockExam(
    val id: String,
    val grade: Int,
    val subject: SubjectType,
    val title: String,
    val subtitle: String,
    val durationMinutes: Int,
    val totalMarks: Int,
    val isBoardExam: Boolean,
    val questions: List<ExamQuestion>
)

@Entity(tableName = "exam_attempts")
data class ExamAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val userName: String,
    val examId: String,
    val examTitle: String,
    val grade: Int,
    val subject: String,
    val score: Int,
    val totalMarks: Int,
    val accuracyPercent: Int,
    val timeTakenSeconds: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val passed: Boolean
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // "PERFORMANCE", "EXAM_RESULT", "CIRCULAR", "ALERT"
    val targetRole: String, // "ALL", "STUDENT", "TEACHER", "PRINCIPAL"
    val targetGrade: Int? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val metric: String? = null
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val threadId: String, // e.g. "student_teacher" or "teacher_principal"
    val senderId: String,
    val senderName: String,
    val senderRole: String,
    val recipientId: String,
    val recipientName: String,
    val recipientRole: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "learning_documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val grade: Int,
    val subject: String,
    val category: String, // "NCERT Solutions", "Board Question Paper", "Formula Sheet", "Mind Map"
    val fileFormat: String = "PDF",
    val fileSizeBytes: String = "2.4 MB",
    val uploadedBy: String,
    val uploaderRole: String,
    val downloadCount: Int = 0,
    val isDownloaded: Boolean = false,
    val summaryPreview: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
