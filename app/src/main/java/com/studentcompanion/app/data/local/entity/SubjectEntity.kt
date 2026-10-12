package com.studentcompanion.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val code: String,
    val colorHex: String,
    val targetAttendancePercentage: Float = 75.0f,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
