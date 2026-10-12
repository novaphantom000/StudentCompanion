package com.studentcompanion.app.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.studentcompanion.app.data.local.dao.StudySessionDao
import com.studentcompanion.app.data.repository.AttendanceRepository

@Composable
fun ProfileScreen(
    sessionDao: StudySessionDao,
    attendanceRepo: AttendanceRepository
) {
    val sessions by sessionDao.getAllSessions().collectAsState(initial = emptyList())
    var selectedTimeRange by remember { mutableStateOf("Weekly") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Profile & Insights Analytics", style = MaterialTheme.typography.headlineLarge)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Student Overview", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Total Study Sessions: ${sessions.size}")
                Text("Total Focus Minutes: ${sessions.sumOf { it.durationMinutes }} mins")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedTimeRange == "Weekly",
                onClick = { selectedTimeRange = "Weekly" },
                label = { Text("Weekly") }
            )
            FilterChip(
                selected = selectedTimeRange == "Monthly",
                onClick = { selectedTimeRange = "Monthly" },
                label = { Text("Monthly") }
            )
            FilterChip(
                selected = selectedTimeRange == "Semester",
                onClick = { selectedTimeRange = "Semester" },
                label = { Text("Semester") }
            )
        }

        Text("Retrospective Session Logs", style = MaterialTheme.typography.titleLarge)

        if (sessions.isEmpty()) {
            Text("No completed study sessions recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sessions) { session ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("${session.durationMinutes} minutes", style = MaterialTheme.typography.titleSmall)
                                Text("Tag: ${session.retrospectiveTag}", style = MaterialTheme.typography.bodySmall)
                            }
                            Text("Day ${session.dateEpochDay}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
