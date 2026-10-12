package com.studentcompanion.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attachment_items",
    foreignKeys = [
        ForeignKey(
            entity = NoteItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("noteId")]
)
data class AttachmentItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val noteId: Long,
    val fileUri: String,
    val fileName: String,
    val fileType: String,
    val extractedText: String? = null,
    val addedAt: Long = System.currentTimeMillis()
)
