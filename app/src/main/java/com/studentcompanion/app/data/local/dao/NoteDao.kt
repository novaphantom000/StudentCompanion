package com.studentcompanion.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.studentcompanion.app.data.local.entity.NoteItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM note_items WHERE subjectId = :subjectId AND isArchived = 0 ORDER BY updatedAt DESC")
    fun getNotesForSubject(subjectId: Long): Flow<List<NoteItemEntity>>

    @Query("SELECT * FROM note_items WHERE isArchived = 0 ORDER BY updatedAt DESC")
    fun getAllActiveNotes(): Flow<List<NoteItemEntity>>

    @Query("SELECT * FROM note_items WHERE id = :id")
    suspend fun getNoteById(id: Long): NoteItemEntity?

    @Query("SELECT * FROM note_items WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    fun searchNotes(query: String): Flow<List<NoteItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteItemEntity): Long

    @Update
    suspend fun updateNote(note: NoteItemEntity)

    @Delete
    suspend fun deleteNote(note: NoteItemEntity)
}
