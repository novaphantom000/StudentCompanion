package com.studentcompanion.app.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.studentcompanion.app.data.local.StudentCompanionDatabase
import com.studentcompanion.app.data.repository.AiRepository
import com.studentcompanion.app.data.repository.AttendanceRepository
import com.studentcompanion.app.data.repository.SpacedRepetitionRepository
import com.studentcompanion.app.data.repository.VaultRepository
import com.studentcompanion.app.ui.attendance.AttendanceScreen
import com.studentcompanion.app.ui.components.DockedMiniplayer
import com.studentcompanion.app.ui.dashboard.DashboardScreen
import com.studentcompanion.app.ui.focus.FocusGuardScreen
import com.studentcompanion.app.ui.profile.ProfileScreen
import com.studentcompanion.app.ui.settings.SettingsScreen
import com.studentcompanion.app.ui.vaults.VaultsScreen

@Composable
fun MainAppNavigation(
    database: StudentCompanionDatabase
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val attendanceRepo = remember { AttendanceRepository(database.subjectDao(), database.attendanceDao()) }
    val vaultRepo = remember { VaultRepository(database.noteDao(), database.attachmentDao()) }
    val recallRepo = remember { SpacedRepetitionRepository(database.spacedRecallDao()) }
    val aiRepo = remember { AiRepository() }

    var miniplayerTitle by remember { mutableStateOf<String?>(null) }
    var isMiniplayerPlaying by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            Column {
                miniplayerTitle?.let { title ->
                    DockedMiniplayer(
                        title = title,
                        isPlaying = isMiniplayerPlaying,
                        onPlayPauseToggle = { isMiniplayerPlaying = !isMiniplayerPlaying }
                    )
                }

                NavigationBar {
                    val items = listOf(
                        Screen.Dashboard to Icons.Default.Dashboard,
                        Screen.Attendance to Icons.Default.CalendarToday,
                        Screen.Vaults to Icons.Default.Folder,
                        Screen.FocusGuard to Icons.Default.Timer,
                        Screen.Profile to Icons.Default.Person,
                        Screen.Settings to Icons.Default.Settings
                    )

                    items.forEach { (screen, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Dashboard.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(attendanceRepo = attendanceRepo, recallRepo = recallRepo)
            }
            composable(Screen.Attendance.route) {
                AttendanceScreen(attendanceRepo = attendanceRepo, timetableDao = database.timetableDao())
            }
            composable(Screen.Vaults.route) {
                VaultsScreen(
                    vaultRepo = vaultRepo,
                    attendanceRepo = attendanceRepo,
                    aiRepo = aiRepo,
                    onStartAudioPlayback = { title ->
                        miniplayerTitle = title
                        isMiniplayerPlaying = true
                    }
                )
            }
            composable(Screen.FocusGuard.route) {
                FocusGuardScreen(
                    subjectDao = database.subjectDao(),
                    sessionDao = database.studySessionDao(),
                    examDao = database.examDao()
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(sessionDao = database.studySessionDao(), attendanceRepo = attendanceRepo)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(database = database)
            }
        }
    }
}
