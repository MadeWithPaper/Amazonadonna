package com.amazonadonna.database

import androidx.room.*
import com.amazonadonna.model.Payout

@Dao
interface PayoutDao {
    @Query("SELECT * FROM payout")
    fun getAll(): List<Payout>

    @Query("SELECT * FROM payout WHERE synced = (:syncState)")
    fun getAllBySyncState(syncState: Int): List<Payout>

    @Query("SELECT * FROM payout WHERE artisanId = (:artisanId) ORDER BY date DESC")
    fun getAllByArtisanId(artisanId: String): List<Payout>

    @Query("SELECT * FROM payout WHERE artisanId IN (:artisanIds) ORDER BY date DESC")
    fun getAllByMultipleArtisanId(artisanIds: List<String>): List<Payout>

    @Query("SELECT * FROM payout WHERE payoutId IN (:payoutIds)")
    fun loadAllByIds(payoutIds: IntArray): List<Payout>

    @Query("UPDATE payout SET synced = (:syncState) WHERE payoutId = (:payoutId)")
    fun setSyncedState(payoutId: String, syncState: Int)

    @Query("SELECT * FROM payout WHERE payoutId LIKE :id " +
            "LIMIT 1")
    fun findByID(id: String): Payout

    @Query("UPDATE payout SET artisanId = :newArtisanId, synced = :syncState WHERE artisanId = :oldArtisanId")
    fun updateArtisanIdAndUnsync(oldArtisanId: String, newArtisanId : String, syncState: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(payouts: List<Payout>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(payout: Payout)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(payout: Payout)

    @Delete
    fun delete(payout: Payout)

    @Query("DELETE FROM payout")
    fun deleteAll()
}