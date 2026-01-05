package com.stepsync.di

import com.stepsync.data.repository.*
import com.stepsync. domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.stepsync.data.repository.FirebaseGoalRepository
import com. stepsync.domain.repository.GoalRepository
import javax.inject.Singleton

/**
 * Hilt module for providing repository instances
 * Updated to use Firebase repositories
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        firebaseAuthRepository: FirebaseAuthRepository
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        firebaseUserRepository: FirebaseUserRepository
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindStepRecordRepository(
        firebaseStepRepository: FirebaseStepRepository
    ): StepRecordRepository

    @Binds
    @Singleton
    abstract fun bindActivityRepository(
        firebaseActivityRepository: FirebaseActivityRepository
    ): ActivityRepository

    @Binds
    @Singleton
    abstract fun bindGoalRepository(
        firebaseGoalRepository: FirebaseGoalRepository
    ): GoalRepository

    @Binds
    @Singleton
    abstract fun bindFriendRepository(
        firebaseFriendRepository: FirebaseFriendRepository
    ): FriendRepository

    @Binds
    @Singleton
    abstract fun bindAchievementRepository(
        firebaseAchievementRepository: FirebaseAchievementRepository
    ): AchievementRepository

    @Binds
    @Singleton
    abstract fun bindChallengeRepository(
        firebaseChallengeRepository: FirebaseChallengeRepository
    ): ChallengeRepository
}