package com.studentcompanion.app

import android.app.Application
import com.studentcompanion.app.core.logger.DebugLogger
import com.studentcompanion.app.core.security.SecurityManager

class StudentCompanionApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DebugLogger.init(this)
        SecurityManager.init(this)
        DebugLogger.i("App", "Student Companion Application Initialized")
    }
}
