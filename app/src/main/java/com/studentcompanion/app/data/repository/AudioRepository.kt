package com.studentcompanion.app.data.repository

import com.studentcompanion.app.data.local.dao.AudioRecordDao
import com.studentcompanion.app.data.local.entity.AudioRecordEntity
import kotlinx.coroutines.flow.Flow

class AudioRepository(
    private val audioRecordDao: AudioRecordDao
) {
    fun getAudioForNote(noteId: Long): Flow<List<AudioRecordEntity>> = audioRecordDao.getAudioForNote(noteId)

    suspend fun saveAudioRecord(record: AudioRecordEntity): Long = audioRecordDao.insertAudioRecord(record)

    suspend fun deleteAudioRecord(record: AudioRecordEntity) = audioRecordDao.deleteAudioRecord(record)
}
