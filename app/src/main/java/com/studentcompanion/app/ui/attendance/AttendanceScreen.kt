package com.studentcompanion.app.ui.attendance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.studentcompanion.app.data.local.dao.TimetableDao
import com.studentcompanion.app.data.local.entity.SubjectEntity
import com.studentcompanion.app.data.repository.AttendanceRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun AttendanceScreen(
    attendanceRepo: AttendanceRepository,
    timetableDao: TimetableDao
) {
    val coroutineScope = rememberCoroutineScope()
    val subjects by attendanceRepo.getAllSubjects().collectAsState(initial = emptyList())
    val todayEpochDay = remember { LocalDate.now().toEpochDay() }

    var showAddSubjectDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSubjectDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Subject")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(text = "Attendance Logging", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(16.dp))

            if (subjects.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No subjects added. Tap + to add a subject.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(subjects) { subject ->
                        SubjectAttendanceRow(
                            subject = subject,
                            onMark = { status ->
                                coroutineScope.launch {
                                    attendanceRepo.markAttendance(
                                        subjectId = subject.id,
                                        dateEpochDay = todayEpochDay,
                                        status = status
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddSubjectDialog) {
        AddSubjectDialog(
            onDismiss = { showAddSubjectDialog = false },
            onConfirm = { name, code, target ->
                coroutineScope.launch {
                    attendanceRepo.addSubject(
                        SubjectEntity(
                            name = name,
                            code = code,
                            colorHex = "#6200EE",
                            targetAttendancePercentage = target
                        )
                    )
                    showAddSubjectDialog = false
                }
            }
        )
    }
}

@Composable
fun SubjectAttendanceRow(
    subject: SubjectEntity,
    onMark: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = subject.name, style = MaterialTheme.typography.titleMedium)
                Text(text = subject.code, style = MaterialTheme.typography.bodySmall)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { onMark("PRESENT") }) {
                    Icon(Icons.Default.Check, contentDescription = "Present", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { onMark("ABSENT") }) {
                    Icon(Icons.Default.Close, contentDescription = "Absent", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AddSubjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, code: String, targetPct: Float) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var targetPct by remember { mutableStateOf("75") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Subject") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subject Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Course Code") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = targetPct,
                    onValueChange = { targetPct = it },
                    label = { Text("Target Attendance %") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val target = targetPct.toFloatOrNull() ?: 75f
                        onConfirm(name, code, target)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
