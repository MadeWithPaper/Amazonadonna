package com.amazonadonna.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.amazonadonna.model.Artisan

@Database(entities = arrayOf(Artisan::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun artisanDao(): ArtisanDao
}