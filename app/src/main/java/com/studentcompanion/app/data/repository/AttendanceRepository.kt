package com.studentcompanion.app.data.repository

import com.studentcompanion.app.data.local.dao.AttendanceDao
import com.studentcompanion.app.data.local.dao.SubjectDao
import com.studentcompanion.app.data.local.entity.AttendanceRecordEntity
import com.studentcompanion.app.data.local.entity.SubjectEntity
import com.studentcompanion.app.domain.model.AttendanceSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.math.ceil

class AttendanceRepository(
    private val subjectDao: SubjectDao,
    private val attendanceDao: AttendanceDao
) {

    fun getAllSubjects(): Flow<List<SubjectEntity>> = subjectDao.getAllActiveSubjects()

    suspend fun addSubject(subject: SubjectEntity): Long = subjectDao.insertSubject(subject)

    suspend fun updateSubject(subject: SubjectEntity) = subjectDao.updateSubject(subject)

    suspend fun deleteSubject(subject: SubjectEntity) = subjectDao.deleteSubject(subject)

    fun getAttendanceSummaries(): Flow<List<AttendanceSummary>> {
        return combine(
            subjectDao.getAllActiveSubjects(),
            attendanceDao.getAllRecords()
        ) { subjects, records ->
            subjects.map { subject ->
                val subjectRecords = records.filter { it.subjectId == subject.id && it.status != "CANCELLED" }
                val attended = subjectRecords.count { it.status == "PRESENT" }
                val total = subjectRecords.size
                val currentPct = if (total > 0) (attended.toFloat() / total) * 100f else 100f
                val buffer = calculateBunkBuffer(attended, total, subject.targetAttendancePercentage)

                AttendanceSummary(
                    subjectId = subject.id,
                    subjectName = subject.name,
                    subjectCode = subject.code,
                    colorHex = subject.colorHex,
                    attendedCount = attended,
                    totalCount = total,
                    currentPercentage = currentPct,
                    targetPercentage = subject.targetAttendancePercentage,
                    bunkBuffer = buffer
                )
            }
        }
    }

    suspend fun markAttendance(subjectId: Long, dateEpochDay: Long, status: String, slotIndex: Int = 0, note: String? = null) {
        val record = AttendanceRecordEntity(
            subjectId = subjectId,
            dateEpochDay = dateEpochDay,
            status = status,
            slotIndex = slotIndex,
            note = note
        )
        attendanceDao.insertRecord(record)
    }

    fun calculateBunkBuffer(attended: Int, total: Int, targetPercentage: Float): Int {
        if (total == 0) return 0
        val targetRatio = targetPercentage / 100f

        return if (attended.toFloat() / total >= targetRatio) {
            val maxTotal = attended / targetRatio
            val buffer = maxTotal - total
            buffer.toInt().coerceAtLeast(0)
        } else {
            val needed = (targetRatio * total - attended) / (1f - targetRatio)
            -ceil(needed).toInt()
        }
    }
}
