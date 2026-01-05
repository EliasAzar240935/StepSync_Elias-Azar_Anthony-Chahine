package com.stepsync.worker

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work. CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google. firebase.firestore.FirebaseFirestore
import com.stepsync.util.Constants
import com.stepsync. util.NotificationHelper
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java. util.*

class DailyReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override suspend fun doWork(): Result {
        return try {
            Log.d("DailyReminderWorker", "🔔 Running daily reminder check...")

            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log. d("DailyReminderWorker", "No user logged in, skipping")
                return Result.success()
            }

            // Get today's date
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Date())

            // Get today's step count
            val stepDocs = firestore.collection("stepRecords")
                .whereEqualTo("userId", currentUser. uid)
                .whereEqualTo("date", today)
                .get()
                .await()

            val currentSteps = stepDocs.documents
                .firstOrNull()
                ?.getLong("steps")
                ?.toInt() ?: 0

            // Get user's goal (default 10,000)
            val userDoc = firestore.collection("users")
                .document(currentUser. uid)
                .get()
                .await()

            val goalSteps = userDoc.getLong("dailyStepGoal")?. toInt() ?: 10000

            // Send reminder notification
            NotificationHelper.sendDailyReminder(context, currentSteps, goalSteps)

            Log.d("DailyReminderWorker", "✅ Sent reminder:  $currentSteps / $goalSteps steps")

            Result.success()
        } catch (e: Exception) {
            Log.e("DailyReminderWorker", "❌ Error sending reminder", e)
            Result.failure()
        }
    }
}