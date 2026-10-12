package com.studentcompanion.app.domain.model

data class AttendanceSummary(
    val subjectId: Long,
    val subjectName: String,
    val subjectCode: String,
    val colorHex: String,
    val attendedCount: Int,
    val totalCount: Int,
    val currentPercentage: Float,
    val targetPercentage: Float,
    val bunkBuffer: Int
)
