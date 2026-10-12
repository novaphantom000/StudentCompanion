package com.studentcompanion.app.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard", "Dashboard")
    object Attendance : Screen("attendance", "Attendance")
    object Vaults : Screen("vaults", "Vaults")
    object FocusGuard : Screen("focus_guard", "Focus Guard")
    object Profile : Screen("profile", "Profile")
    object Settings : Screen("settings", "Settings")
}
