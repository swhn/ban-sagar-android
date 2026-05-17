package com.madebysai.bansagar.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [SlangEntity::class], version = 1, exportSchema = false)
@TypeConverters(SlangConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun slangDao(): SlangDao
}
