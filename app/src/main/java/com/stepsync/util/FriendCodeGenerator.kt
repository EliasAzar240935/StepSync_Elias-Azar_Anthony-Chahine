package com.stepsync.util

import kotlin.random.Random

object FriendCodeGenerator {

    private const val CODE_PREFIX = "STEP"
    private const val CODE_LENGTH = 6
    private const val CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

    /**
     * Generate a unique friend code
     * Format:  STEP-A1B2C3
     */
    fun generateFriendCode(): String {
        val randomPart = (1..CODE_LENGTH)
            .map { CHARACTERS[Random.nextInt(CHARACTERS. length)] }
            .joinToString("")

        return "$CODE_PREFIX-$randomPart"
    }

    /**
     * Validate friend code format
     */
    fun isValidFriendCode(code:  String): Boolean {
        val regex = Regex("^STEP-[A-Z0-9]{6}$")
        return regex.matches(code. uppercase())
    }

    /**
     * Format user input to proper friend code format
     */
    fun formatFriendCode(input: String): String {
        val cleaned = input.replace("-", "").uppercase()
        return if (cleaned.startsWith("STEP")) {
            val code = cleaned.removePrefix("STEP")
            "STEP-$code"
        } else {
            "STEP-$cleaned"
        }
    }
}