package com.stepsync.util

/**
 * Constants used throughout the app
 */
object Constants {
    // User preferences
    const val PREFS_NAME = "step_sync_prefs"
    const val KEY_USER_ID = "user_id"
    const val KEY_IS_LOGGED_IN = "is_logged_in"
    const val KEY_DARK_MODE = "dark_mode"
    const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    
    // Step tracking
    const val DEFAULT_DAILY_STEP_GOAL = 10000
    const val MIN_STEP_GOAL = 1000
    const val MAX_STEP_GOAL = 50000
    
    // Activity types
    const val ACTIVITY_WALKING = "walking"
    const val ACTIVITY_RUNNING = "running"
    const val ACTIVITY_CYCLING = "cycling"
    const val ACTIVITY_GYM = "gym"
    const val ACTIVITY_SWIMMING = "swimming"
    
    // Goal types
    const val GOAL_TYPE_STEPS = "steps"
    const val GOAL_TYPE_DISTANCE = "distance"
    const val GOAL_TYPE_CALORIES = "calories"
    const val GOAL_TYPE_ACTIVITIES = "activities"
    
    // Goal periods
    const val GOAL_PERIOD_DAILY = "daily"
    const val GOAL_PERIOD_WEEKLY = "weekly"
    const val GOAL_PERIOD_MONTHLY = "monthly"
    
    // Fitness goals
    const val FITNESS_GOAL_WEIGHT_LOSS = "weight_loss"
    const val FITNESS_GOAL_MUSCLE_GAIN = "muscle_gain"
    const val FITNESS_GOAL_FITNESS = "fitness"
    const val FITNESS_GOAL_HEALTH = "health"
    
    // Friend status
    const val FRIEND_STATUS_PENDING = "pending"
    const val FRIEND_STATUS_ACCEPTED = "accepted"
    const val FRIEND_STATUS_BLOCKED = "blocked"
    
    // Notifications
    const val NOTIFICATION_CHANNEL_ID = "step_counter_channel"
    const val NOTIFICATION_ID = 1001
    
    // Service
    const val ACTION_START_SERVICE = "com.stepsync.action.START_SERVICE"
    const val ACTION_STOP_SERVICE = "com.stepsync.action.STOP_SERVICE"
    
    // Database
    const val DATABASE_NAME = "step_sync_database"
}
