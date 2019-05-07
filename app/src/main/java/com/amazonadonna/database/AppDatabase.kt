package com.amazonadonna.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.amazonadonna.model.Artisan
import com.amazonadonna.model.Order
import com.amazonadonna.model.Payout
import com.amazonadonna.model.Product

@Database(entities = arrayOf(Artisan::class, Order::class, Product::class, Payout::class), version = 24, exportSchema = false)
@TypeConverters(ProductListTypeConverter::class, PictureListTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun artisanDao(): ArtisanDao
    abstract fun orderDao(): OrderDao
    abstract fun productDao(): ProductDao
    abstract fun payoutDao(): PayoutDao

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