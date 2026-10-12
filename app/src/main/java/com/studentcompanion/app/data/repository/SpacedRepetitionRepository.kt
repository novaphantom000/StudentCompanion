package com.studentcompanion.app.data.repository

import com.studentcompanion.app.data.local.dao.SpacedRecallDao
import com.studentcompanion.app.data.local.entity.SpacedRecallCardEntity
import kotlinx.coroutines.flow.Flow

class SpacedRepetitionRepository(
    private val spacedRecallDao: SpacedRecallDao
) {
    fun getDueCards(todayEpochDay: Long): Flow<List<SpacedRecallCardEntity>> = spacedRecallDao.getDueCards(todayEpochDay)

    fun getCardsForSubject(subjectId: Long): Flow<List<SpacedRecallCardEntity>> = spacedRecallDao.getCardsForSubject(subjectId)

    suspend fun addCard(card: SpacedRecallCardEntity): Long = spacedRecallDao.insertCard(card)

    suspend fun reviewCard(card: SpacedRecallCardEntity, isCorrect: Boolean, todayEpochDay: Long) {
        val newBoxLevel = if (isCorrect) (card.boxLevel + 1).coerceAtMost(5) else 1
        val intervalDays = when (newBoxLevel) {
            1 -> 1
            2 -> 3
            3 -> 7
            4 -> 14
            5 -> 30
            else -> 1
        }
        val isSticky = !isCorrect
        val nextReviewDay = if (isCorrect) todayEpochDay + intervalDays else todayEpochDay

        val updated = card.copy(
            boxLevel = newBoxLevel,
            nextReviewEpochDay = nextReviewDay,
            isStickyUnanswered = isSticky,
            lastReviewedAt = System.currentTimeMillis()
        )
        spacedRecallDao.updateCard(updated)
    }

    suspend fun deleteCard(card: SpacedRecallCardEntity) = spacedRecallDao.deleteCard(card)
}
