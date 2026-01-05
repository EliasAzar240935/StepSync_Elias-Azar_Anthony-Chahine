package com.stepsync.util

import androidx.compose.runtime.Composable
import androidx. compose.ui.platform.LocalConfiguration
import androidx.compose.ui. unit. Dp
import androidx.compose.ui.unit.dp

/**
 * Utility object for responsive design
 */
object ResponsiveUtils {

    /**
     * Window size classes
     */
    enum class WindowSize {
        COMPACT,  // Phones in portrait (< 600dp)
        MEDIUM,   // Tablets in portrait, phones in landscape (600-840dp)
        EXPANDED  // Tablets in landscape (> 840dp)
    }

    /**
     * Get current window size based on screen width
     */
    @Composable
    fun getWindowSize(): WindowSize {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp

        return when {
            screenWidth < 600 -> WindowSize.COMPACT
            screenWidth < 840 -> WindowSize.MEDIUM
            else -> WindowSize.EXPANDED
        }
    }

    /**
     * Get number of columns for grid based on screen size
     */
    @Composable
    fun getGridColumns(): Int {
        return when (getWindowSize()) {
            WindowSize.COMPACT -> 2
            WindowSize.MEDIUM -> 3
            WindowSize.EXPANDED -> 4
        }
    }

    /**
     * Get responsive padding
     */
    @Composable
    fun getContentPadding(): Dp {
        return when (getWindowSize()) {
            WindowSize.COMPACT -> 16.dp
            WindowSize. MEDIUM -> 24.dp
            WindowSize.EXPANDED -> 32.dp
        }
    }

    /**
     * Get max content width (prevents stretching on large screens)
     */
    @Composable
    fun getMaxContentWidth(): Dp {
        return when (getWindowSize()) {
            WindowSize.COMPACT -> Dp.Unspecified
            WindowSize.MEDIUM -> 800.dp
            WindowSize. EXPANDED -> 1200.dp
        }
    }

    /**
     * Get responsive card spacing
     */
    @Composable
    fun getCardSpacing(): Dp {
        return when (getWindowSize()) {
            WindowSize.COMPACT -> 16.dp
            WindowSize. MEDIUM -> 20.dp
            WindowSize. EXPANDED -> 24.dp
        }
    }
}