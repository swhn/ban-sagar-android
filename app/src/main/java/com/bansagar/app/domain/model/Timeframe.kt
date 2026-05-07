package com.bansagar.app.domain.model

import java.time.Instant
import java.time.temporal.ChronoUnit

enum class Timeframe {
    Day, Week, Month, Year;

    fun cutoffIso(): String {
        val days = when (this) {
            Day -> 1L
            Week -> 7L
            Month -> 30L
            Year -> 365L
        }
        return Instant.now().minus(days, ChronoUnit.DAYS).toString()
    }
}
