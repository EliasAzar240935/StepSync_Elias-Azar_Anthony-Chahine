package com.stepsync.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Database module - No longer needed with Firebase
 * Keeping for reference only
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    // Room Database removed - Using Firebase only
}