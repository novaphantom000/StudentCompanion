package com.studentcompanion.app.data.repository

import com.studentcompanion.app.core.logger.DebugLogger
import com.studentcompanion.app.core.security.SecurityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class AiRepository {

    suspend fun askAi(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        if (!SecurityManager.isAiEnabled()) {
            return@withContext Result.failure(IllegalStateException("Remote AI integration is disabled in settings."))
        }

        val apiKey = SecurityManager.getAiApiKey()
        if (apiKey.isNullOrEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("AI API Key is missing."))
        }

        val endpoint = SecurityManager.getAiEndpoint()

        try {
            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer $apiKey")
            conn.doOutput = true
            conn.connectTimeout = 10000
            conn.readTimeout = 10000

            val jsonBody = """
                {
                  "model": "gpt-3.5-turbo",
                  "messages": [{"role": "user", "content": "$prompt"}]
                }
            """.trimIndent()

            OutputStreamWriter(conn.outputStream).use { writer ->
                writer.write(jsonBody)
                writer.flush()
            }

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                Result.success("AI Response received.")
            } else {
                Result.failure(Exception("HTTP ${conn.responseCode}: ${conn.responseMessage}"))
            }
        } catch (e: Exception) {
            DebugLogger.e("AiRepository", "Error during AI request", e)
            Result.failure(e)
        }
    }
}
