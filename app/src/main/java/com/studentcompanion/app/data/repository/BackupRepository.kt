package com.studentcompanion.app.data.repository

import com.google.gson.Gson
import com.studentcompanion.app.core.logger.DebugLogger
import com.studentcompanion.app.data.local.StudentCompanionDatabase
import com.studentcompanion.app.data.local.entity.*
import kotlinx.coroutines.flow.first
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class BackupRepository(
    private val database: StudentCompanionDatabase
) {
    private val gson = Gson()

    data class CompleteBackupData(
        val subjects: List<SubjectEntity>,
        val attendanceRecords: List<AttendanceRecordEntity>,
        val timetableSlots: List<TimetableSlotEntity>,
        val noteItems: List<NoteItemEntity>,
        val attachmentItems: List<AttachmentItemEntity>,
        val audioRecords: List<AudioRecordEntity>,
        val spacedRecallCards: List<SpacedRecallCardEntity>,
        val examItems: List<ExamItemEntity>,
        val studySessions: List<StudySessionEntity>,
        val holidays: List<HolidayEntity>,
        val timestamp: Long = System.currentTimeMillis()
    )

    suspend fun createEncryptedBackup(password: String): ByteArray {
        val notes = database.noteDao().getAllActiveNotes().first()
        val attachments = mutableListOf<AttachmentItemEntity>()
        val audioRecords = mutableListOf<AudioRecordEntity>()

        notes.forEach { note ->
            attachments.addAll(database.attachmentDao().getAttachmentsForNote(note.id).first())
            audioRecords.addAll(database.audioRecordDao().getAudioForNote(note.id).first())
        }

        val backupData = CompleteBackupData(
            subjects = database.subjectDao().getAllSubjects().first(),
            attendanceRecords = database.attendanceDao().getAllRecords().first(),
            timetableSlots = database.timetableDao().getAllSlots().first(),
            noteItems = notes,
            attachmentItems = attachments,
            audioRecords = audioRecords,
            spacedRecallCards = database.spacedRecallDao().getAllCards().first(),
            examItems = database.examDao().getAllExams().first(),
            studySessions = database.studySessionDao().getAllSessions().first(),
            holidays = database.holidayDao().getAllHolidays().first()
        )

        val json = gson.toJson(backupData)

        val salt = ByteArray(16)
        val iv = ByteArray(12)
        SecureRandom().nextBytes(salt)
        SecureRandom().nextBytes(iv)

        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val ciphertext = cipher.doFinal(json.toByteArray(StandardCharsets.UTF_8))

        val output = ByteArrayOutputStream()
        output.write(salt)
        output.write(iv)
        output.write(ciphertext)
        return output.toByteArray()
    }

    suspend fun restoreEncryptedBackup(backupBytes: ByteArray, password: String): Boolean {
        return try {
            val bis = ByteArrayInputStream(backupBytes)
            val salt = ByteArray(16)
            val iv = ByteArray(12)
            if (bis.read(salt) != 16 || bis.read(iv) != 12) return false
            val ciphertext = bis.readBytes()

            val key = deriveKey(password, salt)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
            val jsonBytes = cipher.doFinal(ciphertext)
            val json = String(jsonBytes, StandardCharsets.UTF_8)

            val data = gson.fromJson(json, CompleteBackupData::class.java) ?: return false

            data.subjects.forEach { database.subjectDao().insertSubject(it) }
            data.attendanceRecords.forEach { database.attendanceDao().insertRecord(it) }
            data.timetableSlots.forEach { database.timetableDao().insertSlot(it) }
            data.noteItems.forEach { database.noteDao().insertNote(it) }
            data.attachmentItems.forEach { database.attachmentDao().insertAttachment(it) }
            data.audioRecords.forEach { database.audioRecordDao().insertAudioRecord(it) }
            data.spacedRecallCards.forEach { database.spacedRecallDao().insertCard(it) }
            data.examItems.forEach { database.examDao().insertExam(it) }
            data.studySessions.forEach { database.studySessionDao().insertSession(it) }
            data.holidays.forEach { database.holidayDao().insertHoliday(it) }

            true
        } catch (e: Exception) {
            DebugLogger.e("BackupRepository", "Failed to restore backup", e)
            false
        }
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, 10000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }
}
