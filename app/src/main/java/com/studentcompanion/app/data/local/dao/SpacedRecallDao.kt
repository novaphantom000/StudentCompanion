package com.studentcompanion.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.studentcompanion.app.data.local.entity.SpacedRecallCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpacedRecallDao {
    @Query("SELECT * FROM spaced_recall_cards WHERE subjectId = :subjectId")
    fun getCardsForSubject(subjectId: Long): Flow<List<SpacedRecallCardEntity>>

    @Query("SELECT * FROM spaced_recall_cards WHERE nextReviewEpochDay <= :todayEpochDay OR isStickyUnanswered = 1")
    fun getDueCards(todayEpochDay: Long): Flow<List<SpacedRecallCardEntity>>

    @Query("SELECT * FROM spaced_recall_cards")
    fun getAllCards(): Flow<List<SpacedRecallCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: SpacedRecallCardEntity): Long

    @Update
    suspend fun updateCard(card: SpacedRecallCardEntity)

    @Delete
    suspend fun deleteCard(card: SpacedRecallCardEntity)
}
