package com.stepsync.util

import com.stepsync.data. model.Achievement
import com.stepsync.data.model.AchievementCategory
import com. stepsync.data.model. AchievementTier

/**
 * Centralized achievement definitions
 * All available achievements in the app
 */
object AchievementDefinitions {

    fun getAllDefinitions(): List<Achievement> = listOf(
        // ===== STEP ACHIEVEMENTS =====
        Achievement(
            achievementType = "first_steps",
            title = "First Steps",
            description = "Take your first 100 steps",
            category = AchievementCategory.STEPS,
            tier = AchievementTier.BRONZE,
            requirement = 100,
            points = 10,
            iconName = "footprint"
        ),
        Achievement(
            achievementType = "walker",
            title = "Walker",
            description = "Reach 1,000 steps in a single day",
            category = AchievementCategory.STEPS,
            tier = AchievementTier.BRONZE,
            requirement = 1000,
            points = 10,
            iconName = "walk"
        ),
        Achievement(
            achievementType = "strider",
            title = "Strider",
            description = "Reach 5,000 steps in a single day",
            category = AchievementCategory.STEPS,
            tier = AchievementTier.SILVER,
            requirement = 5000,
            points = 25,
            iconName = "directions_walk"
        ),
        Achievement(
            achievementType = "daily_goal",
            title = "Daily Goal",
            description = "Reach 10,000 steps in a single day",
            category = AchievementCategory.STEPS,
            tier = AchievementTier.GOLD,
            requirement = 10000,
            points = 50,
            iconName = "flag"
        ),
        Achievement(
            achievementType = "super_stepper",
            title = "Super Stepper",
            description = "Reach 15,000 steps in a single day",
            category = AchievementCategory.STEPS,
            tier = AchievementTier.GOLD,
            requirement = 15000,
            points = 50,
            iconName = "trending_up"
        ),
        Achievement(
            achievementType = "marathon_walker",
            title = "Marathon Walker",
            description = "Reach 20,000 steps in a single day",
            category = AchievementCategory.STEPS,
            tier = AchievementTier.PLATINUM,
            requirement = 20000,
            points = 100,
            iconName = "emoji_events"
        ),
        Achievement(
            achievementType = "ultra_walker",
            title = "Ultra Walker",
            description = "Reach 30,000 steps in a single day",
            category = AchievementCategory.STEPS,
            tier = AchievementTier.DIAMOND,
            requirement = 30000,
            points = 250,
            iconName = "military_tech"
        ),

        // ===== CUMULATIVE STEP ACHIEVEMENTS =====
        Achievement(
            achievementType = "total_10k",
            title = "10K Total",
            description = "Accumulate 10,000 total steps",
            category = AchievementCategory.STEPS,
            tier = AchievementTier.BRONZE,
            requirement = 10000,
            points = 10,
            iconName = "check_circle"
        ),
        Achievement(
            achievementType = "total_100k",
            title = "100K Total",
            description = "Accumulate 100,000 total steps",
            category = AchievementCategory.STEPS,
            tier = AchievementTier.SILVER,
            requirement = 100000,
            points = 25,
            iconName = "verified"
        ),
        Achievement(
            achievementType = "total_500k",
            title = "Half Million",
            description = "Accumulate 500,000 total steps",
            category = AchievementCategory.STEPS,
            tier = AchievementTier. GOLD,
            requirement = 500000,
            points = 50,
            iconName = "stars"
        ),
        Achievement(
            achievementType = "total_1m",
            title = "Millionaire",
            description = "Accumulate 1,000,000 total steps",
            category = AchievementCategory.STEPS,
            tier = AchievementTier.PLATINUM,
            requirement = 1000000,
            points = 100,
            iconName = "workspace_premium"
        ),

        // ===== STREAK ACHIEVEMENTS =====
        Achievement(
            achievementType = "streak_3",
            title = "Getting Started",
            description = "Log steps for 3 consecutive days",
            category = AchievementCategory.STREAKS,
            tier = AchievementTier.BRONZE,
            requirement = 3,
            points = 10,
            iconName = "local_fire_department"
        ),
        Achievement(
            achievementType = "streak_7",
            title = "Week Warrior",
            description = "Log steps for 7 consecutive days",
            category = AchievementCategory.STREAKS,
            tier = AchievementTier. SILVER,
            requirement = 7,
            points = 25,
            iconName = "whatshot"
        ),
        Achievement(
            achievementType = "streak_14",
            title = "Two Week Champion",
            description = "Log steps for 14 consecutive days",
            category = AchievementCategory.STREAKS,
            tier = AchievementTier. GOLD,
            requirement = 14,
            points = 50,
            iconName = "local_fire_department"
        ),
        Achievement(
            achievementType = "streak_30",
            title = "Monthly Master",
            description = "Log steps for 30 consecutive days",
            category = AchievementCategory.STREAKS,
            tier = AchievementTier. PLATINUM,
            requirement = 30,
            points = 100,
            iconName = "whatshot"
        ),
        Achievement(
            achievementType = "streak_100",
            title = "Centurion",
            description = "Log steps for 100 consecutive days",
            category = AchievementCategory.STREAKS,
            tier = AchievementTier.DIAMOND,
            requirement = 100,
            points = 250,
            iconName = "military_tech"
        ),

        // ===== ACTIVITY ACHIEVEMENTS =====
        Achievement(
            achievementType = "first_activity",
            title = "Activity Starter",
            description = "Complete your first activity",
            category = AchievementCategory.ACTIVITIES,
            tier = AchievementTier.BRONZE,
            requirement = 1,
            points = 10,
            iconName = "fitness_center"
        ),
        Achievement(
            achievementType = "activity_10",
            title = "Active Lifestyle",
            description = "Complete 10 activities",
            category = AchievementCategory.ACTIVITIES,
            tier = AchievementTier. SILVER,
            requirement = 10,
            points = 25,
            iconName = "sports_score"
        ),
        Achievement(
            achievementType = "activity_50",
            title = "Fitness Enthusiast",
            description = "Complete 50 activities",
            category = AchievementCategory. ACTIVITIES,
            tier = AchievementTier.GOLD,
            requirement = 50,
            points = 50,
            iconName = "emoji_events"
        ),
        Achievement(
            achievementType = "activity_100",
            title = "Activity Master",
            description = "Complete 100 activities",
            category = AchievementCategory.ACTIVITIES,
            tier = AchievementTier.PLATINUM,
            requirement = 100,
            points = 100,
            iconName = "workspace_premium"
        ),

        // ===== GOAL ACHIEVEMENTS =====
        Achievement(
            achievementType = "first_goal",
            title = "Goal Setter",
            description = "Create your first goal",
            category = AchievementCategory.GOALS,
            tier = AchievementTier.BRONZE,
            requirement = 1,
            points = 10,
            iconName = "track_changes"
        ),
        Achievement(
            achievementType = "goal_complete_1",
            title = "Goal Achiever",
            description = "Complete your first goal",
            category = AchievementCategory. GOALS,
            tier = AchievementTier.SILVER,
            requirement = 1,
            points = 25,
            iconName = "check_circle"
        ),
        Achievement(
            achievementType = "goal_complete_5",
            title = "Goal Crusher",
            description = "Complete 5 goals",
            category = AchievementCategory.GOALS,
            tier = AchievementTier.GOLD,
            requirement = 5,
            points = 50,
            iconName = "verified"
        ),
        Achievement(
            achievementType = "goal_complete_10",
            title = "Unstoppable",
            description = "Complete 10 goals",
            category = AchievementCategory.GOALS,
            tier = AchievementTier.PLATINUM,
            requirement = 10,
            points = 100,
            iconName = "emoji_events"
        ),

        // ===== SOCIAL ACHIEVEMENTS =====
        Achievement(
            achievementType = "first_friend",
            title = "Social Butterfly",
            description = "Add your first friend",
            category = AchievementCategory.SOCIAL,
            tier = AchievementTier.BRONZE,
            requirement = 1,
            points = 10,
            iconName = "person_add"
        ),
        Achievement(
            achievementType = "friend_5",
            title = "Friend Circle",
            description = "Have 5 friends",
            category = AchievementCategory. SOCIAL,
            tier = AchievementTier.SILVER,
            requirement = 5,
            points = 25,
            iconName = "groups"
        ),
        Achievement(
            achievementType = "friend_10",
            title = "Popular",
            description = "Have 10 friends",
            category = AchievementCategory.SOCIAL,
            tier = AchievementTier.GOLD,
            requirement = 10,
            points = 50,
            iconName = "group"
        ),
        Achievement(
            achievementType = "challenge_join",
            title = "Challenge Accepted",
            description = "Join your first challenge",
            category = AchievementCategory.SOCIAL,
            tier = AchievementTier.SILVER,
            requirement = 1,
            points = 25,
            iconName = "sports_martial_arts"
        ),
        Achievement(
            achievementType = "challenge_win",
            title = "Challenge Champion",
            description = "Win a challenge",
            category = AchievementCategory.SOCIAL,
            tier = AchievementTier.PLATINUM,
            requirement = 1,
            points = 100,
            iconName = "emoji_events"
        ),

        // ===== SPECIAL ACHIEVEMENTS =====
        Achievement(
            achievementType = "early_bird",
            title = "Early Bird",
            description = "Log steps before 6 AM",
            category = AchievementCategory.SPECIAL,
            tier = AchievementTier.SILVER,
            requirement = 1,
            points = 25,
            iconName = "wb_sunny"
        ),
        Achievement(
            achievementType = "night_owl",
            title = "Night Owl",
            description = "Log steps after 10 PM",
            category = AchievementCategory. SPECIAL,
            tier = AchievementTier.SILVER,
            requirement = 1,
            points = 25,
            iconName = "nightlight"
        ),
        Achievement(
            achievementType = "weekend_warrior",
            title = "Weekend Warrior",
            description = "Reach your goal on both Saturday and Sunday",
            category = AchievementCategory.SPECIAL,
            tier = AchievementTier.GOLD,
            requirement = 1,
            points = 50,
            iconName = "weekend"
        )
    )

    fun getPointsForTier(tier: AchievementTier): Int = when (tier) {
        AchievementTier.BRONZE -> 10
        AchievementTier.SILVER -> 25
        AchievementTier.GOLD -> 50
        AchievementTier. PLATINUM -> 100
        AchievementTier.DIAMOND -> 250
    }
}