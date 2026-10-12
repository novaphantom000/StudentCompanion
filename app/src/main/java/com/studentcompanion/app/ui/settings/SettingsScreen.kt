package com.studentcompanion.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.studentcompanion.app.core.security.SecurityManager
import com.studentcompanion.app.data.local.StudentCompanionDatabase
import com.studentcompanion.app.data.repository.BackupRepository
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    database: StudentCompanionDatabase
) {
    val coroutineScope = rememberCoroutineScope()
    val backupRepo = remember { BackupRepository(database) }

    var appTheme by remember { mutableStateOf(SecurityManager.getAppTheme()) }
    var enableAi by remember { mutableStateOf(SecurityManager.isAiEnabled()) }
    var apiKey by remember { mutableStateOf(SecurityManager.getAiApiKey() ?: "") }
    var autoLock by remember { mutableStateOf(false) }

    var backupPassword by remember { mutableStateOf("") }
    var backupStatusMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings & Submenus", style = MaterialTheme.typography.headlineLarge)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Appearance Customization", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("SYSTEM", "LIGHT", "DARK", "OLED").forEach { theme ->
                        FilterChip(
                            selected = appTheme == theme,
                            onClick = {
                                appTheme = theme
                                SecurityManager.setAppTheme(theme)
                            },
                            label = { Text(theme) }
                        )
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("AI Assistant Settings", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Ask AI (Remote)")
                    Switch(
                        checked = enableAi,
                        onCheckedChange = {
                            enableAi = it
                            SecurityManager.setAiEnabled(it)
                        }
                    )
                }
                if (enableAi) {
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = {
                            apiKey = it
                            SecurityManager.setAiApiKey(it)
                        },
                        label = { Text("Encrypted AI API Key") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Encrypted Backup & Restore", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = backupPassword,
                    onValueChange = { backupPassword = it },
                    label = { Text("Backup Encryption Password") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (backupPassword.isNotBlank()) {
                                coroutineScope.launch {
                                    try {
                                        val backupBytes = backupRepo.createEncryptedBackup(backupPassword)
                                        backupStatusMsg = "Backup created successfully (${backupBytes.size} bytes)."
                                    } catch (e: Exception) {
                                        backupStatusMsg = "Backup failed: ${e.message}"
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Create Backup")
                    }
                }
                backupStatusMsg?.let { msg ->
                    Text(text = msg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("System & Security", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Auto-lock on Background")
                    Switch(
                        checked = autoLock,
                        onCheckedChange = { autoLock = it }
                    )
                }
            }
        }
    }
}
