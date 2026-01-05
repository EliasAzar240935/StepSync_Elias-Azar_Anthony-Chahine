package com.stepsync.util

/**
 * Utility object for fitness calculations
 */
object CalculationUtils {
    // Average step length in meters
    private const val AVERAGE_STEP_LENGTH = 0.762f
    
    // MET values (Metabolic Equivalent of Task) for different activities
    private const val MET_WALKING = 3.5f
    private const val MET_RUNNING = 7.0f
    private const val MET_CYCLING = 6.0f
    private const val MET_GYM = 5.0f
    private const val MET_SWIMMING = 8.0f

    /**
     * Calculate distance from steps
     * @param steps Number of steps
     * @return Distance in meters
     */
    fun calculateDistance(steps: Int): Float {
        return steps * 0.762f
    }

    /**
     * Calculate calories burned from steps
     * @param steps Number of steps
     * @param weight Weight in kg
     * @param height Height in cm
     * @return Calories burned
     */
    fun calculateCaloriesFromSteps(steps: Int, weight: Float, height: Float): Float {
        val distance = calculateDistance(steps)
        val distanceKm = distance / 1000f
        // Simple formula: calories = weight * distance * factor
        return weight * distanceKm * 0.75f
    }

    /**
     * Calculate calories burned for an activity
     * @param activityType Type of activity
     * @param durationSeconds Duration in seconds
     * @param weight Weight in kg
     * @return Calories burned
     */
    fun calculateCaloriesForActivity(
        activityType: String,
        durationSeconds: Long,
        weight: Float
    ): Float {
        val met = when (activityType.lowercase()) {
            "walking" -> MET_WALKING
            "running" -> MET_RUNNING
            "cycling" -> MET_CYCLING
            "gym" -> MET_GYM
            "swimming" -> MET_SWIMMING
            else -> MET_WALKING
        }
        
        val hours = durationSeconds / 3600f
        // Formula: Calories = MET * weight(kg) * hours
        return met * weight * hours
    }

    /**
     * Calculate distance for an activity
     * @param activityType Type of activity
     * @param durationSeconds Duration in seconds
     * @param steps Number of steps (if available)
     * @return Distance in meters
     */
    fun calculateDistanceForActivity(
        activityType: String,
        durationSeconds: Long,
        steps: Int = 0
    ): Float {
        return if (steps > 0) {
            calculateDistance(steps)
        } else {
            // Estimate based on average speed for activity type
            val speed = when (activityType.lowercase()) {
                "walking" -> 5.0f // 5 km/h
                "running" -> 10.0f // 10 km/h
                "cycling" -> 20.0f // 20 km/h
                else -> 5.0f
            }
            val hours = durationSeconds / 3600f
            speed * hours * 1000 // Convert to meters
        }
    }

    /**
     * Calculate BMI
     * @param weight Weight in kg
     * @param height Height in cm
     * @return BMI value
     */
    fun calculateBMI(weight: Float, height: Float): Float {
        val heightM = height / 100f
        return weight / (heightM * heightM)
    }

    /**
     * Format distance for display
     * @param distanceMeters Distance in meters
     * @return Formatted string (km or m)
     */
    fun formatDistance(distanceMeters: Float): String {
        return if (distanceMeters >= 1000) {
            String.format("%.2f km", distanceMeters / 1000)
        } else {
            String.format("%.0f m", distanceMeters)
        }
    }

    /**
     * Format calories for display
     * @param calories Calories burned
     * @return Formatted string
     */
    fun formatCalories(calories: Float): String {
        return String.format("%.0f cal", calories)
    }
}
