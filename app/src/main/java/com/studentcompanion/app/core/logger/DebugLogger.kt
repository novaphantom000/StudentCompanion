package com.studentcompanion.app.core.logger

import android.content.Context
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.regex.Pattern

object DebugLogger {

    private const val TAG = "StudentCompanion"
    private const val MAX_LOG_SIZE_BYTES = 1 * 1024 * 1024
    private const val LOG_FILE_NAME = "debug.log"

    private val executor = Executors.newSingleThreadExecutor()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    private var logFile: File? = null

    private val SENSITIVE_PATTERNS = listOf(
        Pattern.compile("(?i)(api[_-]?key|secret|password|passwd|token|auth|bearer|card[_-]?num|cvv|pin)\\s*[:=]\\s*[\"']?([^\\s\"']+)[\"']?"),
        Pattern.compile("(?i)bearer\\s+[a-zA-Z0-9_\\-\\.~\\+\\/=]+"),
        Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\\b"),
        Pattern.compile("\\b(?:\\d[ -]*?){13,16}\\b")
    )

    fun init(context: Context) {
        executor.execute {
            try {
                val externalDir = context.getExternalFilesDir(null)
                val targetDir = if (externalDir != null && externalDir.canWrite()) {
                    externalDir
                } else {
                    context.filesDir
                }
                logFile = File(targetDir, LOG_FILE_NAME)
                rotateIfNeeded()
            } catch (e: Exception) {
                safeLog("ERROR", TAG, "Failed to initialize DebugLogger file storage", e)
            }
        }
    }

    fun initForTest(testLogFile: File) {
        logFile = testLogFile
        rotateIfNeeded()
    }

    fun d(tag: String, message: String) {
        log("DEBUG", tag, message, null)
    }

    fun i(tag: String, message: String) {
        log("INFO", tag, message, null)
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        log("WARN", tag, message, throwable)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        log("ERROR", tag, message, throwable)
    }

    fun redactSensitiveData(input: String): String {
        var redacted = input
        for (pattern in SENSITIVE_PATTERNS) {
            val matcher = pattern.matcher(redacted)
            if (matcher.find()) {
                redacted = matcher.replaceAll("[REDACTED]")
            }
        }
        return redacted
    }

    private fun log(level: String, tag: String, message: String, throwable: Throwable?) {
        val safeMessage = redactSensitiveData(message)
        val timestamp = dateFormat.format(Date())
        val stackTrace = throwable?.let { getStackTraceString(it) } ?: ""
        val logLine = "$timestamp [$level] [$tag]: $safeMessage${if (stackTrace.isNotEmpty()) "\n$stackTrace" else ""}\n"

        safeLog(level, tag, safeMessage, throwable)

        executor.execute {
            try {
                logFile?.let { file ->
                    rotateIfNeeded()
                    file.appendText(logLine)
                }
            } catch (e: Exception) {
                safeLog("ERROR", TAG, "Failed to write log to file", e)
            }
        }
    }

    private fun safeLog(level: String, tag: String, safeMessage: String, throwable: Throwable?) {
        try {
            when (level) {
                "DEBUG" -> Log.d(tag, safeMessage)
                "INFO" -> Log.i(tag, safeMessage)
                "WARN" -> Log.w(tag, safeMessage, throwable)
                "ERROR" -> Log.e(tag, safeMessage, throwable)
            }
        } catch (_: Throwable) {
        }
    }

    private fun getStackTraceString(throwable: Throwable): String {
        return try {
            Log.getStackTraceString(throwable)
        } catch (_: Throwable) {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            throwable.printStackTrace(pw)
            sw.toString()
        }
    }

    private fun rotateIfNeeded() {
        val file = logFile ?: return
        if (file.exists() && file.length() > MAX_LOG_SIZE_BYTES) {
            val backupFile = File(file.parent, "${file.name}.1")
            if (backupFile.exists()) {
                backupFile.delete()
            }
            file.renameTo(backupFile)
            file.createNewFile()
        }
    }

    fun getLogFile(): File? = logFile
}
