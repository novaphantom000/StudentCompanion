package com.studentcompanion.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.studentcompanion.app.core.security.SecurityManager
import com.studentcompanion.app.data.local.StudentCompanionDatabase
import com.studentcompanion.app.ui.navigation.MainAppNavigation
import com.studentcompanion.app.ui.onboarding.OnboardingScreen
import com.studentcompanion.app.ui.theme.StudentCompanionTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = StudentCompanionDatabase.getInstance(applicationContext)

        setContent {
            var onboardingCompleted by remember { mutableStateOf(SecurityManager.isOnboardingCompleted()) }

            StudentCompanionTheme {
                if (!onboardingCompleted) {
                    OnboardingScreen(
                        onOnboardingFinished = {
                            onboardingCompleted = true
                        }
                    )
                } else {
                    MainAppNavigation(database = database)
                }
            }
        }
    }
}
