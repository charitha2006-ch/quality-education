package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.data.model.DocumentEntity
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentRepositoryScreen(
    currentUser: UserProfile,
    documents: List<DocumentEntity>,
    searchQuery: String,
    filterSubject: String,
    selectedGrade: Int,
    onSearchChange: (String) -> Unit,
    onFilterSubjectChange: (String) -> Unit,
    onDownload: (Long) -> Unit,
    onOpenUploadDialog: () -> Unit
) {
    var previewDoc by remember { mutableStateOf<DocumentEntity?>(null) }

    val filteredDocs = documents.filter { doc ->
        val matchesSearch = doc.title.contains(searchQuery, ignoreCase = true) ||
                doc.description.contains(searchQuery, ignoreCase = true) ||
                doc.subject.contains(searchQuery, ignoreCase = true)
        val matchesSubject = filterSubject == "All" || doc.subject.equals(filterSubject, ignoreCase = true)
        val matchesGrade = doc.grade == selectedGrade
        matchesSearch && matchesSubject && matchesGrade
    }

    Scaffold(
        floatingActionButton = {
            if (currentUser.role == UserRole.TEACHER || currentUser.role == UserRole.PRINCIPAL) {
                ExtendedFloatingActionButton(
                    onClick = onOpenUploadDialog,
                    containerColor = ScholarNavy,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.CloudUpload, contentDescription = "Upload") },
                    text = { Text("Upload Material", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("upload_material_fab")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("document_repository_screen")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Cloud Document Repository",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ScholarNavy
                    )
                    Text(
                        "Class ${selectedGrade}th Curated Study Materials & Past Papers",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = ScienceTeal.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "${filteredDocs.size} files",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ScienceTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search NCERT notes, formula sheets, board PYQs...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_doc_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ScholarNavy,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Subject Filter Chips
            val subjects = listOf("All", "Science", "Mathematics", "Social Science")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(subjects) { subject ->
                    val isSelected = filterSubject.equals(subject, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterSubjectChange(subject) },
                        label = { Text(subject, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ScholarNavy,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("filter_subject_$subject")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Documents List
            if (filteredDocs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No learning documents found for Class ${selectedGrade}th $filterSubject.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredDocs, key = { it.id }) { doc ->
                        DocumentItemCard(
                            doc = doc,
                            onDownload = { onDownload(doc.id) },
                            onPreview = { previewDoc = doc }
                        )
                    }
                }
            }
        }
    }

    previewDoc?.let { doc ->
        DocumentPreviewDialog(doc = doc, onDismiss = { previewDoc = null })
    }
}

@Composable
private fun DocumentItemCard(
    doc: DocumentEntity,
    onDownload: () -> Unit,
    onPreview: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onPreview() }
            .testTag("doc_item_${doc.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PDF/Doc Format Badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ScholarNavy),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = doc.fileFormat,
                        tint = AcademicGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        doc.fileFormat,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = ScholarNavy.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            doc.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ScholarNavy,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        doc.fileSizeBytes,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    doc.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    "Uploaded by ${doc.uploadedBy} (${doc.uploaderRole})",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Download Action Pill
            IconButton(
                onClick = onDownload,
                modifier = Modifier.testTag("download_btn_${doc.id}")
            ) {
                if (doc.isDownloaded) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Downloaded",
                        tint = SuccessGreen,
                        modifier = Modifier.size(26.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.DownloadForOffline,
                        contentDescription = "Download",
                        tint = ScholarNavy,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DocumentPreviewDialog(
    doc: DocumentEntity,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AutoStories,
                    contentDescription = "Document Overview",
                    tint = ScholarNavy,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(doc.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = ScholarNavy.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                        Text("Class ${doc.grade}th", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ScholarNavy, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Surface(color = ScienceTeal.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                        Text(doc.subject, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ScienceTeal, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Surface(color = AcademicGold.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                        Text(doc.category, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AcademicGold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }

                Text(
                    doc.description,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Curriculum Digest Summary:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ScholarNavy)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            doc.summaryPreview,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }

                Text(
                    "Cloud Repository File Size: ${doc.fileSizeBytes} • Verified by ${doc.uploadedBy}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ScholarNavy)
            ) {
                Text("Close")
            }
        }
    )
}

@Composable
fun UploadDocumentDialog(
    initialGrade: Int,
    onDismiss: () -> Unit,
    onUpload: (title: String, description: String, grade: Int, subject: String, category: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var grade by remember { mutableIntStateOf(initialGrade) }
    var subject by remember { mutableStateOf("Science") }
    var category by remember { mutableStateOf("NCERT Solutions") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CloudUpload, contentDescription = "Upload", tint = ScholarNavy)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upload Learning Material", fontWeight = FontWeight.Bold)
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
                    label = { Text("Document Title") },
                    placeholder = { Text("e.g. 10th Science Electricity Notes") },
                    modifier = Modifier.fillMaxWidth().testTag("upload_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Syllabus Coverage") },
                    modifier = Modifier.fillMaxWidth().testTag("upload_desc_input"),
                    maxLines = 3
                )

                // Grade Selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Target Grade: ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    listOf(8, 9, 10).forEach { g ->
                        FilterChip(
                            selected = grade == g,
                            onClick = { grade = g },
                            label = { Text("Class $g") },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }

                // Subject Selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Subject: ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    listOf("Science", "Mathematics", "Social Science").forEach { s ->
                        FilterChip(
                            selected = subject == s,
                            onClick = { subject = s },
                            label = { Text(s.take(6)) },
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }

                // Category Selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Category: ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    listOf("NCERT Solutions", "Formula Sheet", "Board PYQ").forEach { c ->
                        FilterChip(
                            selected = category == c,
                            onClick = { category = c },
                            label = { Text(c.take(8)) },
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onUpload(title.trim(), description.trim(), grade, subject, category)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ScholarNavy),
                modifier = Modifier.testTag("submit_upload_button")
            ) {
                Text("Publish to Repository")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
