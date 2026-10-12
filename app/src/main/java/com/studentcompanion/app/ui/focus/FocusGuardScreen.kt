package com.studentcompanion.app.ui.focus

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.studentcompanion.app.data.local.dao.ExamDao
import com.studentcompanion.app.data.local.dao.SubjectDao
import com.studentcompanion.app.data.local.dao.StudySessionDao
import com.studentcompanion.app.data.local.entity.ExamItemEntity
import com.studentcompanion.app.data.local.entity.SubjectEntity
import com.studentcompanion.app.data.local.entity.StudySessionEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun FocusGuardScreen(
    subjectDao: SubjectDao,
    sessionDao: StudySessionDao,
    examDao: ExamDao
) {
    val coroutineScope = rememberCoroutineScope()
    val todayEpochDay = remember { LocalDate.now().toEpochDay() }
    val subjects by subjectDao.getAllActiveSubjects().collectAsState(initial = emptyList())
    val exams by examDao.getUpcomingExams(todayEpochDay).collectAsState(initial = emptyList())

    var isTimerRunning by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var isEmergencyCramMode by remember { mutableStateOf(false) }

    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (isTimerRunning) {
                delay(1000L)
                elapsedSeconds++
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Focus Guard & Emergency Cram", style = MaterialTheme.typography.headlineLarge)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isEmergencyCramMode) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isEmergencyCramMode) "⚡ Emergency Cram Mode Active" else "Focus Study Timer",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))

                val minutes = elapsedSeconds / 60
                val seconds = elapsedSeconds % 60
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.displayLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { isTimerRunning = !isTimerRunning }) {
                        Text(if (isTimerRunning) "Pause" else "Start Session")
                    }

                    if (elapsedSeconds > 0) {
                        OutlinedButton(
                            onClick = {
                                isTimerRunning = false
                                val mins = (elapsedSeconds / 60).coerceAtLeast(1)
                                coroutineScope.launch {
                                    val targetSubjectId = if (subjects.isNotEmpty()) {
                                        subjects.first().id
                                    } else {
                                        subjectDao.insertSubject(
                                            SubjectEntity(
                                                name = "General Study",
                                                code = "GEN101",
                                                colorHex = "#6200EE",
                                                targetAttendancePercentage = 75f
                                            )
                                        )
                                    }
                                    sessionDao.insertSession(
                                        StudySessionEntity(
                                            subjectId = targetSubjectId,
                                            durationMinutes = mins,
                                            dateEpochDay = todayEpochDay,
                                            retrospectiveTag = if (isEmergencyCramMode) "Cramming" else "Deep Focus"
                                        )
                                    )
                                    elapsedSeconds = 0
                                }
                            }
                        ) {
                            Text("Save & Finish")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                FilterChip(
                    selected = isEmergencyCramMode,
                    onClick = { isEmergencyCramMode = !isEmergencyCramMode },
                    label = { Text("Emergency Cram Mode") }
                )
            }
        }

        Text("Upcoming Exam Inspector", style = MaterialTheme.typography.titleLarge)

        if (exams.isEmpty()) {
            Text("No upcoming exams scheduled.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(exams) { exam ->
                    ExamCard(exam = exam, todayEpochDay = todayEpochDay)
                }
            }
        }
    }
}

@Composable
fun ExamCard(exam: ExamItemEntity, todayEpochDay: Long) {
    val daysLeft = exam.examEpochDay - todayEpochDay

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = exam.examTitle, style = MaterialTheme.typography.titleMedium)
                Text(text = "Location: ${exam.location ?: "TBD"}", style = MaterialTheme.typography.bodySmall)
            }
            Badge {
                Text(text = if (daysLeft >= 0) "$daysLeft days left" else "Passed")
            }
        }
    }
}
