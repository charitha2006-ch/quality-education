package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.EduRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class MainTab(val label: String) {
    DASHBOARD("Dashboard"),
    LESSONS("Lessons"),
    MOCK_EXAMS("Mock Exams"),
    DOCUMENTS("Repository"),
    MESSAGES("Messages")
}

data class ExamSessionState(
    val exam: MockExam,
    val timeRemainingSeconds: Int,
    val currentQuestionIndex: Int = 0,
    val selectedAnswers: Map<Int, Int> = emptyMap(),
    val markedForReview: Set<Int> = emptySet(),
    val isSubmitted: Boolean = false
)

class EduViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EduRepository = EduRepository(AppDatabase.getDatabase(application))

    // Current logged in user profile (Student, Teacher, Principal)
    private val _currentUser = MutableStateFlow(EduRepository.DEFAULT_USERS[0])
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    // Selected grade filter (default 10 for primary focus)
    private val _selectedGrade = MutableStateFlow(10)
    val selectedGrade: StateFlow<Int> = _selectedGrade.asStateFlow()

    // Active bottom/main tab
    private val _currentTab = MutableStateFlow(MainTab.DASHBOARD)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    // Active Mock Exam Session
    private val _examSession = MutableStateFlow<ExamSessionState?>(null)
    val examSession: StateFlow<ExamSessionState?> = _examSession.asStateFlow()

    // Last completed exam attempt for score dialog
    private val _lastExamResult = MutableStateFlow<ExamAttemptEntity?>(null)
    val lastExamResult: StateFlow<ExamAttemptEntity?> = _lastExamResult.asStateFlow()

    // Active interactive lesson
    private val _activeLesson = MutableStateFlow<Lesson?>(null)
    val activeLesson: StateFlow<Lesson?> = _activeLesson.asStateFlow()

    // UI Dialog & Sheet states
    private val _showNotificationSheet = MutableStateFlow(false)
    val showNotificationSheet: StateFlow<Boolean> = _showNotificationSheet.asStateFlow()

    private val _showUploadDialog = MutableStateFlow(false)
    val showUploadDialog: StateFlow<Boolean> = _showUploadDialog.asStateFlow()

    private val _showSendNotificationDialog = MutableStateFlow(false)
    val showSendNotificationDialog: StateFlow<Boolean> = _showSendNotificationDialog.asStateFlow()

    private val _documentSearchQuery = MutableStateFlow("")
    val documentSearchQuery: StateFlow<String> = _documentSearchQuery.asStateFlow()

    private val _documentFilterSubject = MutableStateFlow("All")
    val documentFilterSubject: StateFlow<String> = _documentFilterSubject.asStateFlow()

    // Active chat thread
    private val _activeThreadId = MutableStateFlow("student_teacher")
    val activeThreadId: StateFlow<String> = _activeThreadId.asStateFlow()

    // Reactive data flows
    val allAttempts: StateFlow<List<ExamAttemptEntity>> = repository.examAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotifCount: StateFlow<Int> = repository.unreadNotificationCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val documents: StateFlow<List<DocumentEntity>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentThreadMessages: StateFlow<List<ChatMessageEntity>> = _activeThreadId
        .flatMapLatest { threadId -> repository.getMessagesForThread(threadId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var examTimerJob: Job? = null

    // Switch Role seamlessly between Student, Teacher, Principal
    fun switchRole(role: UserRole) {
        val user = EduRepository.DEFAULT_USERS.firstOrNull { it.role == role }
            ?: EduRepository.DEFAULT_USERS[0]
        _currentUser.value = user
        if (role == UserRole.STUDENT) {
            _selectedGrade.value = 10
            _activeThreadId.value = "student_teacher"
        } else if (role == UserRole.TEACHER) {
            _selectedGrade.value = 10
            _activeThreadId.value = "student_teacher"
        } else {
            _activeThreadId.value = "teacher_principal"
        }
    }

    fun selectGrade(grade: Int) {
        _selectedGrade.value = grade
    }

    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun setDocumentSearchQuery(query: String) {
        _documentSearchQuery.value = query
    }

    fun setDocumentFilterSubject(subject: String) {
        _documentFilterSubject.value = subject
    }

    fun openLesson(lesson: Lesson) {
        _activeLesson.value = lesson
    }

    fun closeLesson() {
        _activeLesson.value = null
    }

    fun startExam(exam: MockExam) {
        examTimerJob?.cancel()
        val totalSeconds = exam.durationMinutes * 60
        _examSession.value = ExamSessionState(
            exam = exam,
            timeRemainingSeconds = totalSeconds,
            currentQuestionIndex = 0
        )
        // Start live countdown timer
        examTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _examSession.value ?: break
                if (current.timeRemainingSeconds <= 1) {
                    submitExam()
                    break
                } else {
                    _examSession.value = current.copy(
                        timeRemainingSeconds = current.timeRemainingSeconds - 1
                    )
                }
            }
        }
    }

    fun selectExamAnswer(questionId: Int, optionIndex: Int) {
        val current = _examSession.value ?: return
        val updated = current.selectedAnswers.toMutableMap()
        updated[questionId] = optionIndex
        _examSession.value = current.copy(selectedAnswers = updated)
    }

    fun toggleMarkForReview(questionId: Int) {
        val current = _examSession.value ?: return
        val updated = current.markedForReview.toMutableSet()
        if (updated.contains(questionId)) {
            updated.remove(questionId)
        } else {
            updated.add(questionId)
        }
        _examSession.value = current.copy(markedForReview = updated)
    }

    fun navigateQuestion(index: Int) {
        val current = _examSession.value ?: return
        if (index in current.exam.questions.indices) {
            _examSession.value = current.copy(currentQuestionIndex = index)
        }
    }

    fun submitExam() {
        examTimerJob?.cancel()
        val session = _examSession.value ?: return
        val exam = session.exam
        val selected = session.selectedAnswers

        var correctCount = 0
        val markPerQ = if (exam.questions.isNotEmpty()) exam.totalMarks / exam.questions.size else 1
        for (q in exam.questions) {
            if (selected[q.id] == q.correctAnswer) {
                correctCount++
            }
        }
        val score = correctCount * markPerQ
        val timeSpent = (exam.durationMinutes * 60) - session.timeRemainingSeconds

        viewModelScope.launch {
            val user = _currentUser.value
            val attempt = repository.recordExamAttempt(
                userId = user.id,
                userName = user.name,
                exam = exam,
                score = score,
                totalMarks = exam.totalMarks,
                timeTakenSeconds = timeSpent
            )
            _lastExamResult.value = attempt
            _examSession.value = null
        }
    }

    fun dismissExamResult() {
        _lastExamResult.value = null
    }

    fun cancelExam() {
        examTimerJob?.cancel()
        _examSession.value = null
    }

    // Notifications
    fun toggleNotificationSheet(open: Boolean) {
        _showNotificationSheet.value = open
    }

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
        }
    }

    fun triggerSimulatedPerformanceUpdate(customGrade: Int? = null) {
        viewModelScope.launch {
            val grade = customGrade ?: _selectedGrade.value
            repository.sendNotification(
                title = "Class ${grade}th Performance Alert: +7.5% Rise",
                message = "New mock analysis indicates strong mastery in Science & Math concepts for Class ${grade}th board candidates.",
                targetRole = "ALL",
                targetGrade = grade,
                type = "PERFORMANCE"
            )
        }
    }

    fun sendTeacherAlert(title: String, message: String, targetGrade: Int) {
        viewModelScope.launch {
            repository.sendNotification(
                title = title,
                message = message,
                targetRole = "STUDENT",
                targetGrade = targetGrade,
                type = "ALERT"
            )
            _showSendNotificationDialog.value = false
        }
    }

    fun broadcastPrincipalCircular(title: String, message: String) {
        viewModelScope.launch {
            repository.sendNotification(
                title = title,
                message = message,
                targetRole = "ALL",
                targetGrade = null,
                type = "CIRCULAR"
            )
            _showSendNotificationDialog.value = false
        }
    }

    fun toggleSendNotificationDialog(open: Boolean) {
        _showSendNotificationDialog.value = open
    }

    // Document Repository
    fun toggleUploadDialog(open: Boolean) {
        _showUploadDialog.value = open
    }

    fun uploadDocument(
        title: String,
        description: String,
        grade: Int,
        subject: String,
        category: String
    ) {
        viewModelScope.launch {
            val user = _currentUser.value
            repository.uploadDocument(
                title = title,
                description = description,
                grade = grade,
                subject = subject,
                category = category,
                uploadedBy = user.name,
                uploaderRole = user.role.label
            )
            _showUploadDialog.value = false
        }
    }

    fun downloadDocument(id: Long) {
        viewModelScope.launch {
            repository.toggleDownloadDocument(id)
        }
    }

    // Secure Messaging
    fun setActiveThread(threadId: String) {
        _activeThreadId.value = threadId
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return
        val user = _currentUser.value
        val threadId = _activeThreadId.value

        val (recipientId, recipientName, recipientRole) = when (threadId) {
            "student_teacher" -> {
                if (user.role == UserRole.STUDENT) {
                    Triple("teacher_1", "Dr. Sunita Verma", "Teacher")
                } else {
                    Triple("student_1", "Aarav Sharma", "Student")
                }
            }
            "teacher_principal" -> {
                if (user.role == UserRole.TEACHER) {
                    Triple("principal_1", "Dr. K. R. Ramanathan", "Principal")
                } else {
                    Triple("teacher_1", "Dr. Sunita Verma", "Teacher")
                }
            }
            else -> {
                if (user.role == UserRole.STUDENT) {
                    Triple("principal_1", "Dr. K. R. Ramanathan", "Principal")
                } else {
                    Triple("student_1", "Aarav Sharma", "Student")
                }
            }
        }

        viewModelScope.launch {
            repository.sendMessage(
                threadId = threadId,
                senderId = user.id,
                senderName = user.name,
                senderRole = user.role.label,
                recipientId = recipientId,
                recipientName = recipientName,
                recipientRole = recipientRole,
                content = content
            )

            // Auto-generate realistic academic response after 1.5 seconds if sent by student
            if (user.role == UserRole.STUDENT) {
                delay(1500)
                val reply = when {
                    content.contains("focal", ignoreCase = true) || content.contains("lens", ignoreCase = true) ->
                        "Dr. Sunita Verma: Excellent observation! Always remember the sign convention for convex lens (+f) and concave lens (-f)."
                    content.contains("exam", ignoreCase = true) || content.contains("date", ignoreCase = true) || content.contains("admit", ignoreCase = true) ->
                        "Dr. Sunita Verma: Your mock test schedules and admit card instructions have been pinned in the Document Repository under Class 10 Board section."
                    content.contains("math", ignoreCase = true) || content.contains("quadratic", ignoreCase = true) ->
                        "Dr. Sunita Verma: In quadratic equations, always test the discriminant D = b^2 - 4ac first to verify real roots exist!"
                    else ->
                        "Dr. Sunita Verma: Message received! Keep practicing the interactive lessons and daily mock tests. You're making solid progress for the 10th Boards."
                }
                repository.sendMessage(
                    threadId = threadId,
                    senderId = recipientId,
                    senderName = recipientName,
                    senderRole = recipientRole,
                    recipientId = user.id,
                    recipientName = user.name,
                    recipientRole = user.role.label,
                    content = reply
                )
            }
        }
    }
}
