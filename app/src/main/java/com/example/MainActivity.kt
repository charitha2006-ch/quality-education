package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserRole
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.EduViewModel
import com.example.ui.viewmodel.MainTab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                EduMainApp()
            }
        }
    }
}

@Composable
fun EduMainApp(viewModel: EduViewModel = viewModel()) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val selectedGrade by viewModel.selectedGrade.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val examSession by viewModel.examSession.collectAsStateWithLifecycle()
    val lastExamResult by viewModel.lastExamResult.collectAsStateWithLifecycle()
    val activeLesson by viewModel.activeLesson.collectAsStateWithLifecycle()
    val showNotificationSheet by viewModel.showNotificationSheet.collectAsStateWithLifecycle()
    val showUploadDialog by viewModel.showUploadDialog.collectAsStateWithLifecycle()
    val showSendNotificationDialog by viewModel.showSendNotificationDialog.collectAsStateWithLifecycle()

    val allAttempts by viewModel.allAttempts.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadNotifCount by viewModel.unreadNotifCount.collectAsStateWithLifecycle()
    val documents by viewModel.documents.collectAsStateWithLifecycle()
    val documentSearchQuery by viewModel.documentSearchQuery.collectAsStateWithLifecycle()
    val documentFilterSubject by viewModel.documentFilterSubject.collectAsStateWithLifecycle()
    val activeThreadId by viewModel.activeThreadId.collectAsStateWithLifecycle()
    val currentThreadMessages by viewModel.currentThreadMessages.collectAsStateWithLifecycle()

    var showRoleDialog by remember { mutableStateOf(false) }

    // High Priority Full Screen: Active Mock Exam Session
    val currentExamSession = examSession
    if (currentExamSession != null) {
        MockExamSessionView(
            session = currentExamSession,
            onAnswerSelected = { qId, optIdx -> viewModel.selectExamAnswer(qId, optIdx) },
            onToggleReview = { qId -> viewModel.toggleMarkForReview(qId) },
            onNavigate = { idx -> viewModel.navigateQuestion(idx) },
            onSubmit = { viewModel.submitExam() },
            onCancel = { viewModel.cancelExam() }
        )
        return
    }

    // High Priority Full Screen: Active Interactive Lesson Viewer
    val currentLesson = activeLesson
    if (currentLesson != null) {
        InteractiveLessonView(
            lesson = currentLesson,
            onClose = { viewModel.closeLesson() }
        )
        return
    }

    // Standard Scaffold View
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            EduTopAppBar(
                currentUser = currentUser,
                selectedGrade = selectedGrade,
                unreadNotifCount = unreadNotifCount,
                onGradeSelected = { viewModel.selectGrade(it) },
                onRoleClick = { showRoleDialog = true },
                onNotificationClick = { viewModel.toggleNotificationSheet(true) }
            )
        },
        bottomBar = {
            EduBottomNavigationBar(
                currentTab = currentTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentTab, label = "TabCrossfade") { tab ->
                when (tab) {
                    MainTab.DASHBOARD -> {
                        when (currentUser.role) {
                            UserRole.STUDENT -> {
                                StudentDashboardScreen(
                                    user = currentUser,
                                    selectedGrade = selectedGrade,
                                    examAttempts = allAttempts,
                                    notifications = notifications,
                                    onStartExam = { viewModel.startExam(it) },
                                    onOpenLesson = { viewModel.openLesson(it) },
                                    onNavigateTab = { viewModel.selectTab(it) }
                                )
                            }
                            UserRole.TEACHER -> {
                                TeacherDashboardScreen(
                                    user = currentUser,
                                    selectedGrade = selectedGrade,
                                    examAttempts = allAttempts,
                                    onSendAlert = { viewModel.toggleSendNotificationDialog(true) },
                                    onUploadMaterial = { viewModel.toggleUploadDialog(true) },
                                    onStartExam = { viewModel.startExam(it) }
                                )
                            }
                            UserRole.PRINCIPAL -> {
                                PrincipalDashboardScreen(
                                    user = currentUser,
                                    onBroadcastCircular = { viewModel.toggleSendNotificationDialog(true) },
                                    onNavigateTab = { viewModel.selectTab(it) }
                                )
                            }
                        }
                    }
                    MainTab.LESSONS -> {
                        LessonsListScreen(
                            selectedGrade = selectedGrade,
                            onOpenLesson = { viewModel.openLesson(it) }
                        )
                    }
                    MainTab.MOCK_EXAMS -> {
                        MockExamsScreen(
                            selectedGrade = selectedGrade,
                            examAttempts = allAttempts,
                            onStartExam = { viewModel.startExam(it) }
                        )
                    }
                    MainTab.DOCUMENTS -> {
                        DocumentRepositoryScreen(
                            currentUser = currentUser,
                            documents = documents,
                            searchQuery = documentSearchQuery,
                            filterSubject = documentFilterSubject,
                            selectedGrade = selectedGrade,
                            onSearchChange = { viewModel.setDocumentSearchQuery(it) },
                            onFilterSubjectChange = { viewModel.setDocumentFilterSubject(it) },
                            onDownload = { viewModel.downloadDocument(it) },
                            onOpenUploadDialog = { viewModel.toggleUploadDialog(true) }
                        )
                    }
                    MainTab.MESSAGES -> {
                        MessagePortalScreen(
                            currentUser = currentUser,
                            activeThreadId = activeThreadId,
                            messages = currentThreadMessages,
                            onSelectThread = { viewModel.setActiveThread(it) },
                            onSendMessage = { viewModel.sendMessage(it) }
                        )
                    }
                }
            }
        }
    }

    // Role Switcher Dialog
    if (showRoleDialog) {
        RoleSwitchDialog(
            currentUser = currentUser,
            onRoleSelect = { role ->
                viewModel.switchRole(role)
                showRoleDialog = false
            },
            onDismiss = { showRoleDialog = false }
        )
    }

    // Performance Exam Result Dialog
    lastExamResult?.let { result ->
        val exam = com.example.data.repository.EduRepository.MOCK_EXAMS.firstOrNull { it.id == result.examId }
        ExamResultDialog(
            attempt = result,
            exam = exam,
            onDismiss = { viewModel.dismissExamResult() }
        )
    }

    // Notification Sheet
    if (showNotificationSheet) {
        NotificationBottomSheet(
            notifications = notifications,
            onDismiss = { viewModel.toggleNotificationSheet(false) },
            onMarkAsRead = { viewModel.markNotificationAsRead(it) },
            onMarkAllRead = { viewModel.markAllNotificationsAsRead() },
            onTriggerSimulation = { viewModel.triggerSimulatedPerformanceUpdate() }
        )
    }

    // Teacher / Principal Upload Learning Material Dialog
    if (showUploadDialog) {
        UploadDocumentDialog(
            initialGrade = selectedGrade,
            onDismiss = { viewModel.toggleUploadDialog(false) },
            onUpload = { title, desc, grade, subject, category ->
                viewModel.uploadDocument(title, desc, grade, subject, category)
            }
        )
    }

    // Send Notification / Alert / Broadcast Dialog
    if (showSendNotificationDialog) {
        SendNotificationDialog(
            currentUser = currentUser,
            initialGrade = selectedGrade,
            onDismiss = { viewModel.toggleSendNotificationDialog(false) },
            onSendAlert = { title, msg, grade ->
                viewModel.sendTeacherAlert(title, msg, grade)
            },
            onBroadcastCircular = { title, msg ->
                viewModel.broadcastPrincipalCircular(title, msg)
            }
        )
    }
}

