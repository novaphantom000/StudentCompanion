package com.studentcompanion.app.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.studentcompanion.app.core.logger.DebugLogger

object SecurityManager {

    private const val PREFS_FILE_NAME = "encrypted_app_settings"

    private var encryptedPrefs: SharedPreferences? = null

    fun init(context: Context) {
        try {
            val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                PREFS_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (t: Throwable) {
            DebugLogger.e("SecurityManager", "Failed to init EncryptedSharedPreferences, fallback to standard SharedPreferences", t)
            try {
                encryptedPrefs = context.getSharedPreferences("fallback_prefs", Context.MODE_PRIVATE)
            } catch (t2: Throwable) {
                DebugLogger.e("SecurityManager", "Failed to init fallback SharedPreferences", t2)
            }
        }
    }

    fun initForTest(testPrefs: SharedPreferences) {
        encryptedPrefs = testPrefs
    }

    fun saveEncryptedString(key: String, value: String) {
        encryptedPrefs?.edit()?.putString(key, value)?.apply()
    }

    fun getEncryptedString(key: String, defaultValue: String? = null): String? {
        return encryptedPrefs?.getString(key, defaultValue) ?: defaultValue
    }

    fun isAiEnabled(): Boolean {
        return encryptedPrefs?.getBoolean("ai_enabled", false) ?: false
    }

    fun setAiEnabled(enabled: Boolean) {
        encryptedPrefs?.edit()?.putBoolean("ai_enabled", enabled)?.apply()
    }

    fun getAiApiKey(): String? {
        return getEncryptedString("ai_api_key")
    }

    fun setAiApiKey(key: String) {
        saveEncryptedString("ai_api_key", key)
    }

    fun getAiEndpoint(): String {
        return getEncryptedString("ai_endpoint", "https://api.openai.com/v1/chat/completions") ?: "https://api.openai.com/v1/chat/completions"
    }

    fun setAiEndpoint(endpoint: String) {
        saveEncryptedString("ai_endpoint", endpoint)
    }

    fun isOnboardingCompleted(): Boolean {
        return encryptedPrefs?.getBoolean("onboarding_completed", false) ?: false
    }

    fun setOnboardingCompleted(completed: Boolean) {
        encryptedPrefs?.edit()?.putBoolean("onboarding_completed", completed)?.apply()
    }

    fun getAppTheme(): String {
        return getEncryptedString("app_theme", "SYSTEM") ?: "SYSTEM"
    }

    fun setAppTheme(theme: String) {
        saveEncryptedString("app_theme", theme)
    }
}
