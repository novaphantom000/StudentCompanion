package com.studentcompanion.app.data.repository

import com.studentcompanion.app.data.local.dao.AttachmentDao
import com.studentcompanion.app.data.local.dao.NoteDao
import com.studentcompanion.app.data.local.entity.AttachmentItemEntity
import com.studentcompanion.app.data.local.entity.NoteItemEntity
import kotlinx.coroutines.flow.Flow

class VaultRepository(
    private val noteDao: NoteDao,
    private val attachmentDao: AttachmentDao
) {
    fun getNotesForSubject(subjectId: Long): Flow<List<NoteItemEntity>> = noteDao.getNotesForSubject(subjectId)

    fun getAllActiveNotes(): Flow<List<NoteItemEntity>> = noteDao.getAllActiveNotes()

    suspend fun getNoteById(id: Long): NoteItemEntity? = noteDao.getNoteById(id)

    suspend fun saveNote(note: NoteItemEntity): Long = noteDao.insertNote(note)

    suspend fun archiveNote(note: NoteItemEntity) {
        noteDao.updateNote(note.copy(isArchived = true, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteNote(note: NoteItemEntity) = noteDao.deleteNote(note)

    fun getAttachmentsForNote(noteId: Long): Flow<List<AttachmentItemEntity>> = attachmentDao.getAttachmentsForNote(noteId)

    suspend fun addAttachment(attachment: AttachmentItemEntity): Long = attachmentDao.insertAttachment(attachment)

    suspend fun importAndIndexDocument(noteId: Long, fileName: String, fileUri: String, fileContent: String, fileType: String): Long {
        val attachment = AttachmentItemEntity(
            noteId = noteId,
            fileUri = fileUri,
            fileName = fileName,
            fileType = fileType,
            extractedText = fileContent
        )
        return attachmentDao.insertAttachment(attachment)
    }

    fun searchNotesAndDocs(query: String): Flow<List<NoteItemEntity>> = noteDao.searchNotes(query)
}
