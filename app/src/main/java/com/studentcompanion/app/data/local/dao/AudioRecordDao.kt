package com.studentcompanion.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.studentcompanion.app.data.local.entity.AudioRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioRecordDao {
    @Query("SELECT * FROM audio_records WHERE noteId = :noteId")
    fun getAudioForNote(noteId: Long): Flow<List<AudioRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudioRecord(record: AudioRecordEntity): Long

    @Delete
    suspend fun deleteAudioRecord(record: AudioRecordEntity)
}
