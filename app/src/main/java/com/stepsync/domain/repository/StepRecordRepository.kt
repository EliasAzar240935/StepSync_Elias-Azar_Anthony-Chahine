package com.stepsync.domain. repository

import com.stepsync.data.model.StepRecord
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for StepRecord operations (Domain layer)
 */
interface StepRecordRepository {
    suspend fun getStepRecordByDate(userId: String, date: String): StepRecord?
    suspend fun insertStepRecord(stepRecord: StepRecord): Long
    fun observeStepRecordByDate(userId: String, date: String): Flow<StepRecord?>
    fun getAllStepRecords(userId: String): Flow<List<StepRecord>>
    fun getStepRecordsBetweenDates(userId: String, startDate: String, endDate: String): Flow<List<StepRecord>>
    fun getRecentStepRecords(userId: String, limit: Int): Flow<List<StepRecord>>
    suspend fun getTotalStepsBetweenDates(userId: String, startDate: String, endDate: String): Int
    suspend fun insertOrUpdateStepRecord(userId: String, date: String, steps: Int, distance: Float, calories: Float)
    suspend fun updateSteps(userId: String, date: String, steps: Int)
}