package com.amazonadonna.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Delete
import com.amazonadonna.model.Artisan
import android.arch.persistence.room.OnConflictStrategy

@Dao
interface ArtisanDao {
    @Query("SELECT * FROM artisan")
    fun getAll(): List<Artisan>

    @Query("SELECT picURL FROM artisan")
    fun getAllImages(): List<String>

    @Query("SELECT * FROM artisan WHERE artisanId IN (:artisanIds)")
    fun loadAllByIds(artisanIds: IntArray): List<Artisan>

    @Query("SELECT * FROM artisan WHERE artisanName LIKE :artisanName " +
            "LIMIT 1")
    fun findByName(artisanName: String): Artisan

    @Query("SELECT * FROM artisan WHERE artisanId LIKE :id " +
            "LIMIT 1")
    fun findByID(id: String): Artisan

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(artisans: List<Artisan>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(artisan: Artisan)

    @Delete
    fun delete(artisan: Artisan)
}