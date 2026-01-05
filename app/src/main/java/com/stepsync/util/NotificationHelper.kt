package com.stepsync.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android. content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.stepsync.MainActivity
import com.stepsync. R

object NotificationHelper {

    private const val CHANNEL_ID_REMINDERS = "step_reminders"
    private const val CHANNEL_ID_PROGRESS = "step_progress"
    private const val CHANNEL_ID_ACHIEVEMENTS = "achievements"

    private const val NOTIFICATION_ID_REMINDER = 1001
    private const val NOTIFICATION_ID_GOAL = 1002
    private const val NOTIFICATION_ID_CHALLENGE = 1003
    private const val NOTIFICATION_ID_ACHIEVEMENT_BASE = 2000

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES. O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ID_REMINDERS,
                    "Daily Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Daily step reminders"
                },
                NotificationChannel(
                    CHANNEL_ID_PROGRESS,
                    "Progress Updates",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Goal and challenge progress updates"
                },
                NotificationChannel(
                    CHANNEL_ID_ACHIEVEMENTS,
                    "Achievements",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Achievement unlocked notifications"
                }
            )

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            channels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }

    fun sendDailyReminder(context: Context, currentSteps: Int, goalSteps: Int) {
        val intent = Intent(context, MainActivity:: class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent. FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val remaining = goalSteps - currentSteps
        val message = when {
            currentSteps >= goalSteps -> "🎉 You've already reached your goal today!"
            remaining < 1000 -> "💪 Just $remaining steps to reach your goal!"
            else -> "👟 You have $remaining steps left today.  Keep moving!"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("StepSync Reminder")
            .setContentText(message)
            .setPriority(NotificationCompat. PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_REMINDER, notification)
        } catch (e:  SecurityException) {
            // Permission not granted
        }
    }

    fun sendGoalCompletedNotification(context: Context, goalName: String, steps: Int) {
        val intent = Intent(context, MainActivity:: class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent. FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_PROGRESS)
            .setSmallIcon(R. drawable.ic_notification)
            .setContentTitle("🎉 Goal Completed!")
            .setContentText("Congratulations!  You've reached your $goalName goal with $steps steps!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_GOAL, notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    fun sendChallengeProgressNotification(context: Context, challengeName: String, rank: Int, totalParticipants: Int) {
        val intent = Intent(context, MainActivity:: class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent. FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val message = when {
            rank == 1 -> "🥇 You're leading the $challengeName challenge!"
            rank <= 3 -> "🏆 You're in ${rank}th place in $challengeName!"
            else -> "You're ranked #$rank out of $totalParticipants in $challengeName"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_PROGRESS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Challenge Update")
            .setContentText(message)
            .setPriority(NotificationCompat. PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_CHALLENGE, notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    /**
     * Send achievement unlock notification
     */
    fun sendAchievementNotification(context: Context, achievementTitle: String, points: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent. getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ACHIEVEMENTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🏆 Achievement Unlocked!")
            .setContentText("$achievementTitle (+$points points)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300, 100, 300))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_ACHIEVEMENT_BASE + achievementTitle.hashCode(),
                notification
            )
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
}