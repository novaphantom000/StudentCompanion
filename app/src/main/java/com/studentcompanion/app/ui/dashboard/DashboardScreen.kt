package com.studentcompanion.app.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.studentcompanion.app.data.repository.AttendanceRepository
import com.studentcompanion.app.data.repository.SpacedRepetitionRepository
import com.studentcompanion.app.domain.model.AttendanceSummary
import com.studentcompanion.app.ui.components.GlassCard
import java.time.LocalDate

@Composable
fun DashboardScreen(
    attendanceRepo: AttendanceRepository,
    recallRepo: SpacedRepetitionRepository
) {
    val summaries by attendanceRepo.getAttendanceSummaries().collectAsState(initial = emptyList())
    val todayEpochDay = remember { LocalDate.now().toEpochDay() }
    val dueCards by recallRepo.getDueCards(todayEpochDay).collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Student Dashboard",
                style = MaterialTheme.typography.headlineLarge
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Current Streak", style = MaterialTheme.typography.titleMedium)
                        Text("5 Days Active", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text("🔥 5", style = MaterialTheme.typography.headlineMedium)
                }
            }
        }

        item {
            Text(
                text = "Attendance Overview & Bunk Buffers",
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (summaries.isEmpty()) {
            item {
                Text(
                    text = "No active subjects added yet. Go to Attendance or Settings to add subjects.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(summaries) { summary ->
                AttendanceSummaryCard(summary = summary)
            }
        }

        item {
            Text(
                text = "Due Recall Cards (${dueCards.size})",
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (dueCards.isEmpty()) {
            item {
                Text(
                    text = "All cards reviewed for today! Great job.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dueCards) { card ->
                        GlassCard(
                            modifier = Modifier
                                .width(200.dp)
                                .height(120.dp)
                                .padding(4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = card.question,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 3
                                )
                                Text(
                                    text = "Box ${card.boxLevel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceSummaryCard(summary: AttendanceSummary) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = summary.subjectName, style = MaterialTheme.typography.titleMedium)
                    Text(text = summary.subjectCode, style = MaterialTheme.typography.bodySmall)
                }
                val bufferText = if (summary.bunkBuffer >= 0) {
                    "Can Bunk: ${summary.bunkBuffer}"
                } else {
                    "Must Attend: ${-summary.bunkBuffer}"
                }
                val bufferColor = if (summary.bunkBuffer >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)

                AssistChip(
                    onClick = {},
                    label = { Text(bufferText, color = bufferColor) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (summary.currentPercentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${summary.attendedCount}/${summary.totalCount} classes (${String.format("%.1f", summary.currentPercentage)}%)",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Target: ${summary.targetPercentage.toInt()}%",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
