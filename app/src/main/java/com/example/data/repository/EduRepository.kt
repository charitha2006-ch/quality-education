package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EduRepository(private val database: AppDatabase) {

    val examAttempts: Flow<List<ExamAttemptEntity>> = database.examAttemptDao().getAllAttempts()
    val allNotifications: Flow<List<NotificationEntity>> = database.notificationDao().getAllNotifications()
    val allDocuments: Flow<List<DocumentEntity>> = database.documentDao().getAllDocuments()
    val unreadNotificationCount: Flow<Int> = database.notificationDao().getUnreadCount()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialDataIfNeeded()
        }
    }

    fun getNotificationsForRole(role: UserRole): Flow<List<NotificationEntity>> {
        return database.notificationDao().getNotificationsForRole(role.name)
    }

    fun getMessagesForThread(threadId: String): Flow<List<ChatMessageEntity>> {
        return database.chatDao().getMessagesForThread(threadId)
    }

    suspend fun recordExamAttempt(
        userId: String,
        userName: String,
        exam: MockExam,
        score: Int,
        totalMarks: Int,
        timeTakenSeconds: Int
    ): ExamAttemptEntity {
        val accuracy = if (totalMarks > 0) (score * 100) / totalMarks else 0
        val attempt = ExamAttemptEntity(
            userId = userId,
            userName = userName,
            examId = exam.id,
            examTitle = exam.title,
            grade = exam.grade,
            subject = exam.subject.displayName,
            score = score,
            totalMarks = totalMarks,
            accuracyPercent = accuracy,
            timeTakenSeconds = timeTakenSeconds,
            timestamp = System.currentTimeMillis(),
            passed = accuracy >= 50
        )
        database.examAttemptDao().insertAttempt(attempt)

        // Automatically dispatch a real-time notification for performance update!
        val gradeLabel = "Class ${exam.grade}th"
        val notification = NotificationEntity(
            title = "Mock Exam Result: $score/$totalMarks in ${exam.subject.displayName}",
            message = "$userName completed $gradeLabel '${exam.title}' with $accuracy% accuracy.",
            type = "EXAM_RESULT",
            targetRole = "ALL",
            targetGrade = exam.grade,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            metric = "$accuracy%"
        )
        database.notificationDao().insertNotification(notification)
        return attempt
    }

    suspend fun sendNotification(
        title: String,
        message: String,
        targetRole: String = "ALL",
        targetGrade: Int? = null,
        type: String = "ALERT"
    ) {
        val notification = NotificationEntity(
            title = title,
            message = message,
            type = type,
            targetRole = targetRole,
            targetGrade = targetGrade,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        database.notificationDao().insertNotification(notification)
    }

    suspend fun markNotificationRead(id: Long) {
        database.notificationDao().markAsRead(id)
    }

    suspend fun markAllNotificationsRead() {
        database.notificationDao().markAllAsRead()
    }

    suspend fun sendMessage(
        threadId: String,
        senderId: String,
        senderName: String,
        senderRole: String,
        recipientId: String,
        recipientName: String,
        recipientRole: String,
        content: String
    ) {
        val msg = ChatMessageEntity(
            threadId = threadId,
            senderId = senderId,
            senderName = senderName,
            senderRole = senderRole,
            recipientId = recipientId,
            recipientName = recipientName,
            recipientRole = recipientRole,
            content = content,
            timestamp = System.currentTimeMillis()
        )
        database.chatDao().insertMessage(msg)
    }

    suspend fun uploadDocument(
        title: String,
        description: String,
        grade: Int,
        subject: String,
        category: String,
        uploadedBy: String,
        uploaderRole: String,
        fileFormat: String = "PDF",
        fileSize: String = "2.4 MB"
    ) {
        val doc = DocumentEntity(
            title = title,
            description = description,
            grade = grade,
            subject = subject,
            category = category,
            fileFormat = fileFormat,
            fileSizeBytes = fileSize,
            uploadedBy = uploadedBy,
            uploaderRole = uploaderRole,
            downloadCount = 0,
            isDownloaded = false,
            summaryPreview = "High-yield comprehensive notes, sample exercises, and key formulas verified for Class ${grade}th curriculum."
        )
        database.documentDao().insertDocument(doc)

        // Dispatch a real-time notification that new material was uploaded!
        sendNotification(
            title = "New Study Material: $title",
            message = "$uploadedBy ($uploaderRole) shared $category for Class ${grade}th $subject.",
            targetRole = "STUDENT",
            targetGrade = grade,
            type = "CIRCULAR"
        )
    }

    suspend fun toggleDownloadDocument(id: Long) {
        database.documentDao().markDownloaded(id)
    }

    private suspend fun seedInitialDataIfNeeded() {
        val currentDocs = database.documentDao().getAllDocuments().first()
        if (currentDocs.isEmpty()) {
            val initialDocs = listOf(
                DocumentEntity(
                    title = "Class 10 CBSE Science 10-Year Solved Board Papers",
                    description = "Comprehensive solved question bank with step-by-step mark distribution for Physics, Chemistry & Biology.",
                    grade = 10,
                    subject = "Science",
                    category = "Board Question Paper",
                    fileFormat = "PDF",
                    fileSizeBytes = "4.6 MB",
                    uploadedBy = "Dr. Sunita Verma",
                    uploaderRole = "Teacher",
                    downloadCount = 184,
                    isDownloaded = true,
                    summaryPreview = "Covers 2014-2025 Board exam papers with official marking scheme and diagram rubrics."
                ),
                DocumentEntity(
                    title = "Class 10 Mathematics Complete Formula & Theorem Digest",
                    description = "All theorems (Thales, Pythagoras proof), quadratic formula, trigonometry identities and surface area formulas.",
                    grade = 10,
                    subject = "Mathematics",
                    category = "Formula Sheet",
                    fileFormat = "PDF",
                    fileSizeBytes = "2.1 MB",
                    uploadedBy = "Dr. Sunita Verma",
                    uploaderRole = "Teacher",
                    downloadCount = 312,
                    isDownloaded = false,
                    summaryPreview = "Quick reference guide for high scoring in Section A & B of Class 10 Math board exam."
                ),
                DocumentEntity(
                    title = "Class 10 NCERT Exemplar Solutions - Science & Math",
                    description = "In-depth conceptual problems, assertion-reasoning questions, and case-study based questions.",
                    grade = 10,
                    subject = "Science",
                    category = "NCERT Solutions",
                    fileFormat = "PDF",
                    fileSizeBytes = "5.8 MB",
                    uploadedBy = "Dr. K. R. Ramanathan",
                    uploaderRole = "Principal",
                    downloadCount = 220,
                    isDownloaded = false,
                    summaryPreview = "Exemplar questions critical for topping CBSE 10th standard board examinations."
                ),
                DocumentEntity(
                    title = "Class 10 Social Science Board Mind Maps & Timeline Guide",
                    description = "Visual chronology of Indian Nationalism, European Unification, and Mineral & Energy maps.",
                    grade = 10,
                    subject = "Social Science",
                    category = "Mind Map",
                    fileFormat = "PDF",
                    fileSizeBytes = "3.2 MB",
                    uploadedBy = "Dr. Sunita Verma",
                    uploaderRole = "Teacher",
                    downloadCount = 145,
                    isDownloaded = false,
                    summaryPreview = "Visual memory aids for quick retention of history dates, geography maps, and civics institutions."
                ),
                DocumentEntity(
                    title = "Class 9 Science NCERT Lab Manual & Concept Maps",
                    description = "Laws of Motion, Cell organelles, Atoms & Molecules with structured revision tables.",
                    grade = 9,
                    subject = "Science",
                    category = "NCERT Solutions",
                    fileFormat = "PDF",
                    fileSizeBytes = "3.4 MB",
                    uploadedBy = "Dr. Sunita Verma",
                    uploaderRole = "Teacher",
                    downloadCount = 98,
                    isDownloaded = false,
                    summaryPreview = "Foundation building notes for 9th grade students progressing to 10th board prep."
                ),
                DocumentEntity(
                    title = "Class 8 Mathematics & Science Foundation Workbook",
                    description = "Linear equations in one variable, algebraic identities, and Cell biology foundation.",
                    grade = 8,
                    subject = "Mathematics",
                    category = "Formula Sheet",
                    fileFormat = "PDF",
                    fileSizeBytes = "1.8 MB",
                    uploadedBy = "Dr. Sunita Verma",
                    uploaderRole = "Teacher",
                    downloadCount = 76,
                    isDownloaded = false,
                    summaryPreview = "Bridge course materials to reinforce 8th grade fundamental analytical concepts."
                )
            )
            database.documentDao().insertAll(initialDocs)
        }

        val currentNotifs = database.notificationDao().getAllNotifications().first()
        if (currentNotifs.isEmpty()) {
            val initialNotifs = listOf(
                NotificationEntity(
                    title = "Class 10 Board Mock 2026 Live",
                    message = "Science and Standard Mathematics full-length Board simulator tests are now active. Check your preparation level.",
                    type = "CIRCULAR",
                    targetRole = "ALL",
                    targetGrade = 10,
                    timestamp = System.currentTimeMillis() - 3600000 * 4,
                    metric = "Active"
                ),
                NotificationEntity(
                    title = "Performance Milestone: 88% Class Average",
                    message = "Class 10A achieved an outstanding 88% average in Quadratic Equations and Lens Optics practice quizzes.",
                    type = "PERFORMANCE",
                    targetRole = "ALL",
                    targetGrade = 10,
                    timestamp = System.currentTimeMillis() - 3600000 * 8,
                    metric = "88%"
                ),
                NotificationEntity(
                    title = "Remedial Clinic Announced for Class 9",
                    message = "Special doubt resolution session for Motion and Polynomials scheduled on Friday 3:00 PM.",
                    type = "ALERT",
                    targetRole = "TEACHER",
                    targetGrade = 9,
                    timestamp = System.currentTimeMillis() - 3600000 * 12
                ),
                NotificationEntity(
                    title = "Principal's Advisory: Pre-Board Time Table",
                    message = "The official Pre-Board time table has been uploaded to the document repository for Classes 8, 9, and 10.",
                    type = "CIRCULAR",
                    targetRole = "ALL",
                    targetGrade = null,
                    timestamp = System.currentTimeMillis() - 3600000 * 24
                )
            )
            database.notificationDao().insertAll(initialNotifs)
        }

        val currentChats = database.chatDao().getAllMessages().first()
        if (currentChats.isEmpty()) {
            val initialChats = listOf(
                ChatMessageEntity(
                    threadId = "student_teacher",
                    senderId = "teacher_1",
                    senderName = "Dr. Sunita Verma",
                    senderRole = "Teacher",
                    recipientId = "student_1",
                    recipientName = "Aarav Sharma",
                    recipientRole = "Student",
                    content = "Hello Aarav! I reviewed your last Science mock test. Your optics ray diagrams were precise. Make sure to double check sign conventions in the concave mirror questions.",
                    timestamp = System.currentTimeMillis() - 3600000 * 5
                ),
                ChatMessageEntity(
                    threadId = "student_teacher",
                    senderId = "student_1",
                    senderName = "Aarav Sharma",
                    senderRole = "Student",
                    recipientId = "teacher_1",
                    recipientName = "Dr. Sunita Verma",
                    recipientRole = "Teacher",
                    content = "Thank you ma'am! I had a quick question regarding the focal length sign when using a convex lens with a virtual image. Is f always positive?",
                    timestamp = System.currentTimeMillis() - 3600000 * 3
                ),
                ChatMessageEntity(
                    threadId = "student_teacher",
                    senderId = "teacher_1",
                    senderName = "Dr. Sunita Verma",
                    senderRole = "Teacher",
                    recipientId = "student_1",
                    recipientName = "Aarav Sharma",
                    recipientRole = "Student",
                    content = "Yes! The focal length of a convex lens is always positive (+f) according to Cartesian sign convention, regardless of whether the image is real or virtual.",
                    timestamp = System.currentTimeMillis() - 3600000 * 2
                ),
                ChatMessageEntity(
                    threadId = "teacher_principal",
                    senderId = "principal_1",
                    senderName = "Dr. K. R. Ramanathan",
                    senderRole = "Principal",
                    recipientId = "teacher_1",
                    recipientName = "Dr. Sunita Verma",
                    recipientRole = "Teacher",
                    content = "Dr. Verma, what is the current Class 10 Board readiness percentage across the three sections?",
                    timestamp = System.currentTimeMillis() - 3600000 * 6
                ),
                ChatMessageEntity(
                    threadId = "teacher_principal",
                    senderId = "teacher_1",
                    senderName = "Dr. Sunita Verma",
                    senderRole = "Teacher",
                    recipientId = "principal_1",
                    recipientName = "Dr. K. R. Ramanathan",
                    recipientRole = "Principal",
                    content = "Sir, Class 10 readiness is currently at 89.2%. Section A and B are above 90%, and Section C has shown 12% improvement after our weekly mock exam cycles.",
                    timestamp = System.currentTimeMillis() - 3600000 * 4
                ),
                ChatMessageEntity(
                    threadId = "student_principal",
                    senderId = "student_1",
                    senderName = "Aarav Sharma",
                    senderRole = "Student",
                    recipientId = "principal_1",
                    recipientName = "Dr. K. R. Ramanathan",
                    recipientRole = "Principal",
                    content = "Respected Principal Sir, when will the Class 10 board exam roll numbers and admit cards be distributed?",
                    timestamp = System.currentTimeMillis() - 3600000 * 10
                ),
                ChatMessageEntity(
                    threadId = "student_principal",
                    senderId = "principal_1",
                    senderName = "Dr. K. R. Ramanathan",
                    senderRole = "Principal",
                    recipientId = "student_1",
                    recipientName = "Aarav Sharma",
                    recipientRole = "Student",
                    content = "Dear Aarav, the CBSE board roll numbers and authenticated admit cards will be handed over this Thursday in the main auditorium. Keep up your diligent preparation!",
                    timestamp = System.currentTimeMillis() - 3600000 * 8
                )
            )
            database.chatDao().insertAll(initialChats)
        }

        val currentAttempts = database.examAttemptDao().getAllAttempts().first()
        if (currentAttempts.isEmpty()) {
            val initialAttempts = listOf(
                ExamAttemptEntity(
                    userId = "student_1",
                    userName = "Aarav Sharma",
                    examId = "exam_10_sci_1",
                    examTitle = "Class 10 CBSE Board Mock - Science",
                    grade = 10,
                    subject = "Science",
                    score = 36,
                    totalMarks = 40,
                    accuracyPercent = 90,
                    timeTakenSeconds = 840,
                    timestamp = System.currentTimeMillis() - 3600000 * 24,
                    passed = true
                ),
                ExamAttemptEntity(
                    userId = "student_1",
                    userName = "Aarav Sharma",
                    examId = "exam_10_math_1",
                    examTitle = "Class 10 Standard Math Mock",
                    grade = 10,
                    subject = "Mathematics",
                    score = 34,
                    totalMarks = 40,
                    accuracyPercent = 85,
                    timeTakenSeconds = 950,
                    timestamp = System.currentTimeMillis() - 3600000 * 48,
                    passed = true
                ),
                ExamAttemptEntity(
                    userId = "student_2",
                    userName = "Priya Patel",
                    examId = "exam_10_sci_1",
                    examTitle = "Class 10 CBSE Board Mock - Science",
                    grade = 10,
                    subject = "Science",
                    score = 38,
                    totalMarks = 40,
                    accuracyPercent = 95,
                    timeTakenSeconds = 790,
                    timestamp = System.currentTimeMillis() - 3600000 * 20,
                    passed = true
                ),
                ExamAttemptEntity(
                    userId = "student_3",
                    userName = "Rohan Gupta",
                    examId = "exam_10_math_1",
                    examTitle = "Class 10 Standard Math Mock",
                    grade = 10,
                    subject = "Mathematics",
                    score = 22,
                    totalMarks = 40,
                    accuracyPercent = 55,
                    timeTakenSeconds = 1100,
                    timestamp = System.currentTimeMillis() - 3600000 * 18,
                    passed = true
                )
            )
            for (attempt in initialAttempts) {
                database.examAttemptDao().insertAttempt(attempt)
            }
        }
    }

    // Static educational catalog with Grade 8, 9, and heavy Grade 10 Board focus
    companion object {
        val MOCK_EXAMS: List<MockExam> = listOf(
            MockExam(
                id = "exam_10_sci_1",
                grade = 10,
                subject = SubjectType.SCIENCE,
                title = "Class 10 Board Mock: Science (Full Syllabus)",
                subtitle = "Optics, Chemical Reactions, Electricity & Heredity",
                durationMinutes = 15,
                totalMarks = 40,
                isBoardExam = true,
                questions = listOf(
                    ExamQuestion(
                        id = 1,
                        question = "An object is placed at 20 cm in front of a concave mirror of focal length 15 cm. What is the nature and position of the image?",
                        options = listOf(
                            "Real, inverted, formed at -60 cm",
                            "Virtual, erect, formed at +60 cm",
                            "Real, inverted, formed at -30 cm",
                            "Virtual, erect, formed at +30 cm"
                        ),
                        correctAnswer = 0,
                        explanation = "Using mirror formula 1/f = 1/v + 1/u. With u = -20 cm and f = -15 cm: 1/v = -1/15 - (-1/20) = -1/60, so v = -60 cm. Negative sign indicates real and inverted image.",
                        topic = "Optics & Reflection"
                    ),
                    ExamQuestion(
                        id = 2,
                        question = "Which of the following represents a balanced chemical equation for the reaction of iron with steam?",
                        options = listOf(
                            "3Fe + 4H2O(g) → Fe3O4 + 4H2",
                            "Fe + H2O → FeO + H2",
                            "2Fe + 3H2O → Fe2O3 + 3H2",
                            "3Fe + 2H2O → Fe3O2 + 2H2"
                        ),
                        correctAnswer = 0,
                        explanation = "Iron reacts with steam to yield magnetic iron oxide (Fe3O4) and hydrogen gas: 3Fe(s) + 4H2O(g) → Fe3O4(s) + 4H2(g).",
                        topic = "Chemical Reactions"
                    ),
                    ExamQuestion(
                        id = 3,
                        question = "A piece of wire of resistance R is cut into 5 equal parts. These parts are then connected in parallel. If the equivalent resistance is R', what is the ratio R/R'?",
                        options = listOf("25", "5", "1/5", "1/25"),
                        correctAnswer = 0,
                        explanation = "Each piece has resistance r = R/5. When 5 such resistors are connected in parallel: 1/R' = 5 * (1/r) = 5 * (5/R) = 25/R. Therefore, R/R' = 25.",
                        topic = "Electricity"
                    ),
                    ExamQuestion(
                        id = 4,
                        question = "In pea plants, a pure tall plant (TT) is crossed with a short plant (tt). The ratio of pure tall to hybrid tall plants in the F2 generation is:",
                        options = listOf("1:2", "3:1", "1:1", "2:1"),
                        correctAnswer = 0,
                        explanation = "The F2 genotypic ratio is 1 TT (pure tall) : 2 Tt (hybrid tall) : 1 tt (pure dwarf). Thus the ratio of pure tall to hybrid tall is 1:2.",
                        topic = "Heredity & Genetics"
                    )
                )
            ),
            MockExam(
                id = "exam_10_math_1",
                grade = 10,
                subject = SubjectType.MATHEMATICS,
                title = "Class 10 Standard Mathematics Board Mock",
                subtitle = "Quadratic Equations, Trigonometry, Triangles & AP",
                durationMinutes = 15,
                totalMarks = 40,
                isBoardExam = true,
                questions = listOf(
                    ExamQuestion(
                        id = 1,
                        question = "If one root of the quadratic equation 2x² + kx - 6 = 0 is 2, what is the value of k?",
                        options = listOf("-1", "1", "-2", "2"),
                        correctAnswer = 0,
                        explanation = "Substitute x = 2 into the equation: 2(2)² + k(2) - 6 = 0 => 8 + 2k - 6 = 0 => 2k + 2 = 0 => k = -1.",
                        topic = "Quadratic Equations"
                    ),
                    ExamQuestion(
                        id = 2,
                        question = "If sin θ + cos θ = √2 cos θ, then the value of (cos θ - sin θ) is:",
                        options = listOf("√2 sin θ", "√2 cos θ", "2 sin θ", "sin θ"),
                        correctAnswer = 0,
                        explanation = "From sin θ = (√2 - 1) cos θ, multiply both sides by (√2 + 1) to get (√2 + 1) sin θ = cos θ => √2 sin θ = cos θ - sin θ.",
                        topic = "Trigonometry"
                    ),
                    ExamQuestion(
                        id = 3,
                        question = "The 11th term of the arithmetic progression -3, -1/2, 2, ... is:",
                        options = listOf("22", "28", "-20", "20"),
                        correctAnswer = 0,
                        explanation = "First term a = -3, common difference d = -1/2 - (-3) = 5/2. The 11th term a11 = a + 10d = -3 + 10*(5/2) = -3 + 25 = 22.",
                        topic = "Arithmetic Progressions"
                    ),
                    ExamQuestion(
                        id = 4,
                        question = "In triangle ABC, DE || BC such that AD/DB = 3/5. If AC = 5.6 cm, find AE:",
                        options = listOf("2.1 cm", "3.5 cm", "1.8 cm", "2.8 cm"),
                        correctAnswer = 0,
                        explanation = "By Basic Proportionality Theorem, AD/AB = AE/AC. Here AD/AB = 3/(3+5) = 3/8. Thus AE = (3/8) * 5.6 = 2.1 cm.",
                        topic = "Triangles & BPT"
                    )
                )
            ),
            MockExam(
                id = "exam_10_sst_1",
                grade = 10,
                subject = SubjectType.SOCIAL_SCIENCE,
                title = "Class 10 Social Science Board Booster",
                subtitle = "Nationalism in India, Federalism, Resources",
                durationMinutes = 10,
                totalMarks = 30,
                isBoardExam = true,
                questions = listOf(
                    ExamQuestion(
                        id = 1,
                        question = "Why did Mahatma Gandhi decide to withdraw the Non-Cooperation Movement in February 1922?",
                        options = listOf(
                            "Due to the violent Chauri Chaura incident",
                            "Due to the Jallianwala Bagh massacre",
                            "Due to the Simon Commission arrival",
                            "Due to the Poona Pact signing"
                        ),
                        correctAnswer = 0,
                        explanation = "Gandhiji felt the movement was turning violent in many places, especially after demonstrators clashed with police in Chauri Chaura.",
                        topic = "Nationalism in India"
                    ),
                    ExamQuestion(
                        id = 2,
                        question = "Which tier of government was added to the Indian federal system by the 73rd and 74th Constitutional Amendments of 1992?",
                        options = listOf(
                            "Panchayati Raj & Municipalities (Local Government)",
                            "Inter-State Council",
                            "Central Vigilance Commission",
                            "Union Territory Governors"
                        ),
                        correctAnswer = 0,
                        explanation = "The 73rd and 74th amendments established a constitutional third tier of government for Panchayats and Municipalities.",
                        topic = "Federalism"
                    )
                )
            ),
            MockExam(
                id = "exam_9_sci_1",
                grade = 9,
                subject = SubjectType.SCIENCE,
                title = "Class 9 Foundation Test: Laws of Motion & Atoms",
                subtitle = "Newtonian Mechanics and Atomic Structure",
                durationMinutes = 10,
                totalMarks = 25,
                isBoardExam = false,
                questions = listOf(
                    ExamQuestion(
                        id = 1,
                        question = "An object of mass 2 kg is sliding with a constant velocity of 4 m/s on a frictionless horizontal table. What force is required to keep it moving with the same velocity?",
                        options = listOf("0 N", "8 N", "2 N", "4 N"),
                        correctAnswer = 0,
                        explanation = "According to Newton's First Law of Motion, an object maintains constant velocity when net external force is zero.",
                        topic = "Laws of Motion"
                    ),
                    ExamQuestion(
                        id = 2,
                        question = "Which subatomic particle was discovered by J.J. Thomson via the cathode ray tube experiment?",
                        options = listOf("Electron", "Proton", "Neutron", "Positron"),
                        correctAnswer = 0,
                        explanation = "J.J. Thomson discovered the negatively charged electron in 1897.",
                        topic = "Structure of the Atom"
                    )
                )
            ),
            MockExam(
                id = "exam_8_math_1",
                grade = 8,
                subject = SubjectType.MATHEMATICS,
                title = "Class 8 Diagnostic: Linear Equations & Cell Biology",
                subtitle = "Core Foundations for Upper Secondary",
                durationMinutes = 10,
                totalMarks = 20,
                isBoardExam = false,
                questions = listOf(
                    ExamQuestion(
                        id = 1,
                        question = "Solve for x: 5x + 9 = 5 + 3x",
                        options = listOf("x = -2", "x = 2", "x = -7", "x = 7"),
                        correctAnswer = 0,
                        explanation = "5x - 3x = 5 - 9 => 2x = -4 => x = -2.",
                        topic = "Linear Equations"
                    ),
                    ExamQuestion(
                        id = 2,
                        question = "Which organelle is universally known as the powerhouse of the cell?",
                        options = listOf("Mitochondria", "Ribosome", "Golgi Apparatus", "Lysosome"),
                        correctAnswer = 0,
                        explanation = "Mitochondria generate ATP via cellular respiration, powering cellular activities.",
                        topic = "Cell Structure"
                    )
                )
            )
        )

        val INTERACTIVE_LESSONS: List<Lesson> = listOf(
            Lesson(
                id = "lesson_10_sci_optics",
                grade = 10,
                subject = SubjectType.SCIENCE,
                chapter = "Chapter 10: Light - Reflection & Refraction",
                title = "Spherical Lenses & Mirror Sign Conventions",
                summary = "Master concave & convex lens ray diagrams, focal length calculations, and magnification conventions required for CBSE 10th Board.",
                readingTimeMinutes = 8,
                keyTakeaways = listOf(
                    "Convex Lens is converging; focal length is always positive (+f).",
                    "Concave Lens is diverging; focal length is always negative (-f).",
                    "Lens Formula: 1/f = 1/v - 1/u (Take u as negative for real objects).",
                    "Magnification m = v/u = h'/h. Negative m means real/inverted; positive m means virtual/erect."
                ),
                interactiveToolType = InteractiveToolType.LENS_REFRACTION_SIM,
                practiceQuestion = "If an object is placed 30 cm in front of a convex lens of focal length 20 cm, what is the image distance v?",
                practiceOptions = listOf("+60 cm (Real)", "+30 cm (Virtual)", "+15 cm (Real)", "-60 cm (Virtual)"),
                correctOptionIndex = 0,
                explanation = "1/v = 1/f + 1/u = 1/20 + 1/(-30) = 1/20 - 1/30 = (3 - 2)/60 = 1/60. Hence v = +60 cm (Real, inverted image on the opposite side).",
                isCompleted = true
            ),
            Lesson(
                id = "lesson_10_sci_ohms",
                grade = 10,
                subject = SubjectType.SCIENCE,
                chapter = "Chapter 12: Electricity",
                title = "Ohm's Law, Resistance & Joule's Heating",
                summary = "Understand the fundamental relationship between electric potential, current, resistance, and heat dissipation in electric circuits.",
                readingTimeMinutes = 7,
                keyTakeaways = listOf(
                    "Ohm's Law: V = I * R at constant temperature.",
                    "Resistance R = ρ * (L / A), where ρ is resistivity.",
                    "Series: R_total = R1 + R2 + R3. Parallel: 1/R_total = 1/R1 + 1/R2.",
                    "Joule's Heating Law: H = I² * R * t."
                ),
                interactiveToolType = InteractiveToolType.OHMS_LAW_SIMULATOR,
                practiceQuestion = "If voltage across a 5 Ω resistor is increased from 10 V to 20 V, what happens to the electric current?",
                practiceOptions = listOf("Doubles from 2 A to 4 A", "Halves from 4 A to 2 A", "Remains constant", "Quadruples to 8 A"),
                correctOptionIndex = 0,
                explanation = "Since I = V/R, when V doubles at constant R, current I also doubles from 10/5 = 2 A to 20/5 = 4 A.",
                isCompleted = false
            ),
            Lesson(
                id = "lesson_10_math_quad",
                grade = 10,
                subject = SubjectType.MATHEMATICS,
                chapter = "Chapter 4: Quadratic Equations",
                title = "Discriminant, Nature of Roots & Factorization",
                summary = "Explore quadratic equations in standard form ax² + bx + c = 0, investigate the discriminant D = b² - 4ac, and factor polynomials.",
                readingTimeMinutes = 9,
                keyTakeaways = listOf(
                    "Standard form: ax² + bx + c = 0 (a ≠ 0).",
                    "Discriminant D = b² - 4ac.",
                    "If D > 0: Two distinct real roots; if D = 0: Two equal real roots; if D < 0: No real roots.",
                    "Quadratic Formula: x = (-b ± √D) / (2a)."
                ),
                interactiveToolType = InteractiveToolType.QUADRATIC_SOLVER,
                practiceQuestion = "What is the nature of roots for 2x² - 4x + 3 = 0?",
                practiceOptions = listOf("No real roots (D < 0)", "Two equal real roots (D = 0)", "Two distinct real roots (D > 0)", "Rational roots"),
                correctOptionIndex = 0,
                explanation = "D = b² - 4ac = (-4)² - 4(2)(3) = 16 - 24 = -8. Since D < 0, there are no real roots.",
                isCompleted = false
            ),
            Lesson(
                id = "lesson_10_sci_chem",
                grade = 10,
                subject = SubjectType.SCIENCE,
                chapter = "Chapter 1: Chemical Reactions & Equations",
                title = "Balancing Redox & Precipitation Reactions",
                summary = "Master the Law of Conservation of Mass by balancing chemical equations and identifying oxidizing and reducing agents.",
                readingTimeMinutes = 6,
                keyTakeaways = listOf(
                    "Total mass of reactants equals total mass of products.",
                    "Combination: A + B → AB.",
                    "Decomposition: AB → A + B (requires heat/light/electricity).",
                    "Redox: Oxidation is gain of oxygen/loss of electrons; reduction is loss of oxygen/gain of electrons."
                ),
                interactiveToolType = InteractiveToolType.CHEMICAL_BALANCER,
                practiceQuestion = "In the reaction CuO + H2 → Cu + H2O, which substance is being reduced?",
                practiceOptions = listOf("CuO", "H2", "Cu", "H2O"),
                correctOptionIndex = 0,
                explanation = "CuO loses oxygen to form Cu, so CuO is reduced. H2 gains oxygen, so H2 is oxidized.",
                isCompleted = false
            ),
            Lesson(
                id = "lesson_9_sci_motion",
                grade = 9,
                subject = SubjectType.SCIENCE,
                chapter = "Chapter 8: Motion",
                title = "Uniform & Accelerated Motion Graphs",
                summary = "Analyze distance-time and velocity-time graphs and derive the 3 equations of motion: v = u + at, s = ut + 1/2at², v² = u² + 2as.",
                readingTimeMinutes = 7,
                keyTakeaways = listOf(
                    "Slope of distance-time graph represents speed.",
                    "Slope of velocity-time graph represents acceleration.",
                    "Area under velocity-time graph represents displacement.",
                    "Three kinematic formulas apply to uniform acceleration."
                ),
                interactiveToolType = InteractiveToolType.NONE,
                practiceQuestion = "A bus starts from rest and accelerates uniformly at 0.1 m/s² for 2 minutes. What speed does it acquire?",
                practiceOptions = listOf("12 m/s", "0.2 m/s", "6 m/s", "24 m/s"),
                correctOptionIndex = 0,
                explanation = "u = 0, a = 0.1 m/s², t = 2 min = 120 s. v = u + at = 0 + 0.1 * 120 = 12 m/s.",
                isCompleted = false
            ),
            Lesson(
                id = "lesson_8_sci_cell",
                grade = 8,
                subject = SubjectType.SCIENCE,
                chapter = "Chapter 8: Cell - Structure and Functions",
                title = "Plant vs Animal Cell Organelles",
                summary = "Discover the basic structural and functional unit of life, cell wall, cell membrane, nucleus, chloroplast, and vacuole.",
                readingTimeMinutes = 6,
                keyTakeaways = listOf(
                    "Cell membrane gives boundary and regulates substance transport.",
                    "Plant cells have an outer rigid cell wall made of cellulose.",
                    "Chloroplasts contain green pigment chlorophyll for photosynthesis.",
                    "Nucleus contains chromosomes bearing genes for inheritance."
                ),
                interactiveToolType = InteractiveToolType.CELL_ORGANELLES,
                practiceQuestion = "Which organelle is present exclusively in plant cells and absent in animal cells?",
                practiceOptions = listOf("Cell Wall and Plastids", "Mitochondria", "Nucleus", "Ribosomes"),
                correctOptionIndex = 0,
                explanation = "Cell walls and plastids (chloroplasts) are unique to plant cells providing rigidity and photosynthetic capability.",
                isCompleted = true
            )
        )

        val DEFAULT_USERS = listOf(
            UserProfile(
                id = "student_1",
                name = "Aarav Sharma",
                role = UserRole.STUDENT,
                grade = 10,
                email = "aarav.sharma@edugrade.org",
                avatarColorHex = 0xFF1B3B6F,
                designation = "Class 10-A (CBSE Candidate)"
            ),
            UserProfile(
                id = "teacher_1",
                name = "Dr. Sunita Verma",
                role = UserRole.TEACHER,
                grade = 10,
                email = "sunita.verma@edugrade.org",
                avatarColorHex = 0xFF0D9488,
                designation = "Senior Science HOD & Board Mentor"
            ),
            UserProfile(
                id = "principal_1",
                name = "Dr. K. R. Ramanathan",
                role = UserRole.PRINCIPAL,
                grade = null,
                email = "principal@edugrade.org",
                avatarColorHex = 0xFFD97706,
                designation = "Head of School & Academic Council Chair"
            )
        )
    }
}
