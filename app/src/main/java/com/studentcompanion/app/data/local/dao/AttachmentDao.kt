package com.studentcompanion.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.studentcompanion.app.data.local.entity.AttachmentItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {
    @Query("SELECT * FROM attachment_items WHERE noteId = :noteId")
    fun getAttachmentsForNote(noteId: Long): Flow<List<AttachmentItemEntity>>

    @Query("SELECT * FROM attachment_items WHERE extractedText LIKE '%' || :query || '%' OR fileName LIKE '%' || :query || '%'")
    fun searchAttachments(query: String): Flow<List<AttachmentItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: AttachmentItemEntity): Long

    @Delete
    suspend fun deleteAttachment(attachment: AttachmentItemEntity)
}
