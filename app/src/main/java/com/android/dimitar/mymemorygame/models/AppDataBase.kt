package com.android.dimitar.mymemorygame.models

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Records::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordsDao(): RecordsDao
}