package com.bansagar.app.domain.model

enum class Timeframe {
    Day, Week, Month, Year;

    fun days(): Int = when (this) {
        Day -> 1
        Week -> 7
        Month -> 30
        Year -> 365
    }
}
