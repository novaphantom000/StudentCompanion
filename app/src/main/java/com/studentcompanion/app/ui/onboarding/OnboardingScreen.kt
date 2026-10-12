package com.studentcompanion.app.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.studentcompanion.app.core.security.SecurityManager

@Composable
fun OnboardingScreen(
    onOnboardingFinished: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }

    var selectedTheme by remember { mutableStateOf("SYSTEM") }
    var selectedTargetAttendance by remember { mutableFloatStateOf(75f) }
    var enableAi by remember { mutableStateOf(false) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to Student Companion",
                style = MaterialTheme.typography.headlineMedium
            )

            AnimatedContent(targetState = step, label = "OnboardingStep") { currentStep ->
                when (currentStep) {
                    1 -> OnboardingStepAppearance(selectedTheme = selectedTheme, onThemeSelected = { selectedTheme = it })
                    2 -> OnboardingStepAttendance(targetPct = selectedTargetAttendance, onTargetChanged = { selectedTargetAttendance = it })
                    3 -> OnboardingStepModules(enableAi = enableAi, onAiToggled = { enableAi = it })
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (step > 1) {
                    OutlinedButton(onClick = { step-- }) {
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = {
                        if (step < 3) {
                            step++
                        } else {
                            SecurityManager.setAppTheme(selectedTheme)
                            SecurityManager.setAiEnabled(enableAi)
                            SecurityManager.setOnboardingCompleted(true)
                            onOnboardingFinished()
                        }
                    }
                ) {
                    Text(if (step == 3) "Get Started" else "Next")
                }
            }
        }
    }
}

@Composable
private fun OnboardingStepAppearance(selectedTheme: String, onThemeSelected: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Step 1: Choose Appearance", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = selectedTheme == "SYSTEM", onClick = { onThemeSelected("SYSTEM") }, label = { Text("System") })
            FilterChip(selected = selectedTheme == "LIGHT", onClick = { onThemeSelected("LIGHT") }, label = { Text("Light") })
            FilterChip(selected = selectedTheme == "DARK", onClick = { onThemeSelected("DARK") }, label = { Text("Dark") })
            FilterChip(selected = selectedTheme == "OLED", onClick = { onThemeSelected("OLED") }, label = { Text("OLED") })
        }
    }
}

@Composable
private fun OnboardingStepAttendance(targetPct: Float, onTargetChanged: (Float) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Step 2: Default Attendance Target", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text("${targetPct.toInt()}%", style = MaterialTheme.typography.headlineLarge)
        Slider(
            value = targetPct,
            onValueChange = onTargetChanged,
            valueRange = 50f..95f,
            steps = 8
        )
    }
}

@Composable
private fun OnboardingStepModules(enableAi: Boolean, onAiToggled: (Boolean) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Step 3: Optional Features", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text("Enable Ask AI (Remote)", style = MaterialTheme.typography.bodyLarge)
                Text("Requires explicit API key and network connection", style = MaterialTheme.typography.bodySmall)
            }
            Switch(checked = enableAi, onCheckedChange = onAiToggled)
        }
    }
}
