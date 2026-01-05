package com.stepsync.util

import android.content.Context
import android.util.Log
import androidx.work.*
import com.stepsync.worker.DailyReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    private const val WORK_NAME = "daily_step_reminder"
    private const val TAG = "ReminderScheduler"

    /**
     * Schedule daily reminder at specific time (default: 8 PM)
     */
    fun scheduleDailyReminder(
        context: Context,
        hourOfDay: Int = 20,  // 8 PM (20:00)
        minute: Int = 0
    ) {
        try {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val initialDelay = calculateInitialDelay(hourOfDay, minute)

            val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
                repeatInterval = 1,
                repeatIntervalTimeUnit = TimeUnit. DAYS
            )
                .setConstraints(constraints)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .addTag(WORK_NAME)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,  // Don't reschedule if already exists
                dailyWorkRequest
            )

            Log.d(TAG, "✅ Daily reminder scheduled for $hourOfDay:${minute. toString().padStart(2, '0')}")
            Log.d(TAG, "⏱️ Initial delay: ${initialDelay / 1000 / 60} minutes")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to schedule reminder", e)
        }
    }

    /**
     * Cancel all scheduled reminders
     */
    fun cancelDailyReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        Log.d(TAG, "🚫 Daily reminders cancelled")
    }

    /**
     * Calculate delay until next scheduled time
     */
    private fun calculateInitialDelay(hourOfDay: Int, minute: Int): Long {
        val currentTime = Calendar.getInstance()
        val scheduledTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar. SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If scheduled time has passed today, schedule for tomorrow
        if (scheduledTime.before(currentTime)) {
            scheduledTime.add(Calendar.DAY_OF_YEAR, 1)
        }

        return scheduledTime.timeInMillis - currentTime.timeInMillis
    }

    /**
     * Check if reminder is already scheduled
     */
    fun isReminderScheduled(context: Context): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(WORK_NAME)
            .get()

        return workInfos.any { ! it.state.isFinished }
    }
}