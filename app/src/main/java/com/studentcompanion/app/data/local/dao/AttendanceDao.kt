package com.studentcompanion.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.studentcompanion.app.data.local.entity.AttendanceRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records WHERE subjectId = :subjectId")
    fun getRecordsForSubject(subjectId: Long): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE dateEpochDay = :dateEpochDay")
    fun getRecordsForDate(dateEpochDay: Long): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records")
    fun getAllRecords(): Flow<List<AttendanceRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AttendanceRecordEntity): Long

    @Update
    suspend fun updateRecord(record: AttendanceRecordEntity)

    @Delete
    suspend fun deleteRecord(record: AttendanceRecordEntity)

    @Query("DELETE FROM attendance_records WHERE subjectId = :subjectId AND dateEpochDay = :dateEpochDay AND slotIndex = :slotIndex")
    suspend fun deleteSlotRecord(subjectId: Long, dateEpochDay: Long, slotIndex: Int)
}
