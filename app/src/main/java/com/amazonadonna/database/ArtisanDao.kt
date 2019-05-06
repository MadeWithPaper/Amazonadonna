package com.amazonadonna.database

import androidx.room.*
import com.amazonadonna.model.Artisan
import com.amazonadonna.sync.Synchronizer

@Dao
interface ArtisanDao {
    @Query("SELECT * FROM artisan")
    fun getAll(): List<Artisan>

    @Query("SELECT picURL FROM artisan")
    fun getAllImages(): List<String>

    @Query("SELECT * FROM artisan WHERE synced = (:syncState)")
    fun getAllBySyncState(syncState: Int): List<Artisan>

    @Query("SELECT * FROM artisan WHERE synced != (:syncState)")
    fun getAllWithoutSyncState(syncState: Int): List<Artisan>

    @Query("SELECT * FROM artisan WHERE artisanId IN (:artisanIds)")
    fun loadAllByIds(artisanIds: IntArray): List<Artisan>

    @Query("UPDATE artisan SET synced = (:syncState) WHERE artisanId = (:artisanId)")
    fun setSyncedState(artisanId: String, syncState: Int)

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

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(artisan: Artisan)

    @Delete
    fun delete(artisan: Artisan)

    @Query("DELETE FROM artisan")
    fun deleteAll()
}