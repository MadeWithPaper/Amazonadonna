package com.amazonadonna.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import com.amazonadonna.model.Artisan
import com.amazonadonna.model.Order
import com.amazonadonna.model.Product

@Database(entities = arrayOf(Artisan::class, Order::class, Product::class), version = 18, exportSchema = false)
@TypeConverters(ProductListTypeConverter::class, PictureListTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun artisanDao(): ArtisanDao
    abstract fun orderDao(): OrderDao
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "amazonadonna-main"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                return instance
            }
        }
    }
}