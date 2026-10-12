package com.studentcompanion.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.studentcompanion.app.data.local.entity.ExamItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {
    @Query("SELECT * FROM exam_items WHERE examEpochDay >= :todayEpochDay ORDER BY examEpochDay ASC")
    fun getUpcomingExams(todayEpochDay: Long): Flow<List<ExamItemEntity>>

    @Query("SELECT * FROM exam_items ORDER BY examEpochDay ASC")
    fun getAllExams(): Flow<List<ExamItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamItemEntity): Long

    @Update
    suspend fun updateExam(exam: ExamItemEntity)

    @Delete
    suspend fun deleteExam(exam: ExamItemEntity)
}
