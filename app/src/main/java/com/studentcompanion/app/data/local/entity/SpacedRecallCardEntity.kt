package com.studentcompanion.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "spaced_recall_cards",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId")]
)
data class SpacedRecallCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val question: String,
    val answer: String,
    val boxLevel: Int = 1,
    val nextReviewEpochDay: Long = 0,
    val isStickyUnanswered: Boolean = false,
    val lastReviewedAt: Long = 0
)
