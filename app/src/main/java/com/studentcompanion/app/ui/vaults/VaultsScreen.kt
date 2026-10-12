package com.studentcompanion.app.ui.vaults

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.studentcompanion.app.data.local.entity.NoteItemEntity
import com.studentcompanion.app.data.local.entity.SubjectEntity
import com.studentcompanion.app.data.repository.AiRepository
import com.studentcompanion.app.data.repository.AttendanceRepository
import com.studentcompanion.app.data.repository.VaultRepository
import kotlinx.coroutines.launch

@Composable
fun VaultsScreen(
    vaultRepo: VaultRepository,
    attendanceRepo: AttendanceRepository,
    aiRepo: AiRepository,
    onStartAudioPlayback: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val subjects by attendanceRepo.getAllSubjects().collectAsState(initial = emptyList())
    val notes by vaultRepo.getAllActiveNotes().collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showAiDrawer by remember { mutableStateOf(false) }
    var aiPrompt by remember { mutableStateOf("") }
    var aiResponse by remember { mutableStateOf<String?>(null) }
    var isAiLoading by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallFloatingActionButton(onClick = { showAiDrawer = true }) {
                    Icon(Icons.Default.Psychology, contentDescription = "Ask AI")
                }
                FloatingActionButton(onClick = { showAddNoteDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Note")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Subject Vaults & Notes", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search notes, PDFs, handwriting OCR") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            val filteredNotes = notes.filter { note ->
                note.title.contains(searchQuery, ignoreCase = true) || note.content.contains(searchQuery, ignoreCase = true)
            }

            if (filteredNotes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No notes found. Tap + to create a note.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredNotes) { note ->
                        NoteCard(
                            note = note,
                            onPlayAudio = { onStartAudioPlayback(note.title) }
                        )
                    }
                }
            }
        }
    }

    if (showAddNoteDialog) {
        AddNoteDialog(
            subjects = subjects,
            onDismiss = { showAddNoteDialog = false },
            onConfirm = { subjectId, title, content ->
                coroutineScope.launch {
                    vaultRepo.saveNote(
                        NoteItemEntity(
                            subjectId = subjectId,
                            title = title,
                            content = content
                        )
                    )
                    showAddNoteDialog = false
                }
            },
            onCreateSubject = { name, code ->
                coroutineScope.launch {
                    attendanceRepo.addSubject(
                        SubjectEntity(
                            name = name,
                            code = code,
                            colorHex = "#6200EE",
                            targetAttendancePercentage = 75f
                        )
                    )
                }
            }
        )
    }

    if (showAiDrawer) {
        ModalBottomSheet(onDismissRequest = { showAiDrawer = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Ask AI (Contextual Assistant)", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = aiPrompt,
                    onValueChange = { aiPrompt = it },
                    label = { Text("Ask anything about your notes") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        isAiLoading = true
                        coroutineScope.launch {
                            val res = aiRepo.askAi(aiPrompt)
                            aiResponse = res.getOrElse { err -> err.localizedMessage ?: "Failed to connect to AI." }
                            isAiLoading = false
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    enabled = !isAiLoading
                ) {
                    Text(if (isAiLoading) "Thinking..." else "Send")
                }

                aiResponse?.let { resp ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(text = resp, modifier = Modifier.padding(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun NoteCard(
    note: NoteItemEntity,
    onPlayAudio: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = note.title, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onPlayAudio) {
                    Icon(Icons.Default.Mic, contentDescription = "Play Audio Recording")
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = note.content, style = MaterialTheme.typography.bodyMedium, maxLines = 3)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteDialog(
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onConfirm: (subjectId: Long, title: String, content: String) -> Unit,
    onCreateSubject: (name: String, code: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf<Long?>(subjects.firstOrNull()?.id) }

    var newSubjectName by remember { mutableStateOf("") }
    var newSubjectCode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Note") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (subjects.isEmpty()) {
                    Text("No subject found. Enter details to create a subject:", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(value = newSubjectName, onValueChange = { newSubjectName = it }, label = { Text("Subject Name") })
                    OutlinedTextField(value = newSubjectCode, onValueChange = { newSubjectCode = it }, label = { Text("Course Code") })
                } else {
                    Text("Select Subject:", style = MaterialTheme.typography.bodySmall)
                    subjects.forEach { subject ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedSubjectId == subject.id,
                                onClick = { selectedSubjectId = subject.id }
                            )
                            Text("${subject.name} (${subject.code})")
                        }
                    }
                }

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Content") }, minLines = 3)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        if (subjects.isEmpty()) {
                            if (newSubjectName.isNotBlank()) {
                                onCreateSubject(newSubjectName, newSubjectCode)
                            }
                        } else if (selectedSubjectId != null) {
                            onConfirm(selectedSubjectId!!, title, content)
                        }
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
