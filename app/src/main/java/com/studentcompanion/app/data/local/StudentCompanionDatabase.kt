package com.studentcompanion.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.studentcompanion.app.data.local.dao.*
import com.studentcompanion.app.data.local.entity.*

@Database(
    entities = [
        SubjectEntity::class,
        AttendanceRecordEntity::class,
        TimetableSlotEntity::class,
        NoteItemEntity::class,
        AttachmentItemEntity::class,
        AudioRecordEntity::class,
        SpacedRecallCardEntity::class,
        ExamItemEntity::class,
        StudySessionEntity::class,
        HolidayEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class StudentCompanionDatabase : RoomDatabase() {

    abstract fun subjectDao(): SubjectDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun timetableDao(): TimetableDao
    abstract fun noteDao(): NoteDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun audioRecordDao(): AudioRecordDao
    abstract fun spacedRecallDao(): SpacedRecallDao
    abstract fun examDao(): ExamDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun holidayDao(): HolidayDao

    companion object {
        @Volatile
        private var INSTANCE: StudentCompanionDatabase? = null

        fun getInstance(context: Context): StudentCompanionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudentCompanionDatabase::class.java,
                    "student_companion_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
