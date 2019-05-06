package com.amazonadonna.database

import androidx.room.*
import com.amazonadonna.model.Product

@Dao
interface ProductDao {
    @Query("SELECT * FROM product")
    fun getAll(): List<Product>

    @Query("SELECT pictureURLs FROM product")
    fun getAllImages(): List<String>

    @Query("SELECT * FROM product WHERE synced = (:syncState)")
    fun getAllBySyncState(syncState: Int): List<Product>

    @Query("SELECT * FROM product WHERE artisanId = (:artisanId) AND synced != (:syncState)")
    fun getAllByArtisanIdWithoutSyncState(artisanId: String, syncState: Int): List<Product>

    @Query("SELECT * FROM product WHERE itemId IN (:productIds)")
    fun loadAllByIds(productIds: IntArray): List<Product>

    @Query("SELECT * FROM product WHERE artisanId = (:artisanId)")
    fun getAllByArtisanId(artisanId: String): List<Product>

    @Query("UPDATE product SET synced = (:syncState) WHERE itemId = (:productId)")
    fun setSyncedState(productId: String, syncState: Int)

    @Query("DELETE FROM product WHERE itemId=:id")
    fun deleteById(id: String)

    @Query("SELECT * FROM product WHERE itemName LIKE :productName " +
            "LIMIT 1")
    fun findByName(productName: String): Product

    @Query("SELECT * FROM product WHERE itemId LIKE :id " +
            "LIMIT 1")
    fun findByID(id: String): Product

    @Query("UPDATE product SET artisanId = :newArtisanId WHERE artisanId = :oldArtisanId")
    fun updateArtisanId(oldArtisanId: String, newArtisanId : String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(products: List<Product>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(product: Product)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(product: Product)

    @Delete
    fun delete(product: Product)

    @Query("DELETE FROM product")
    fun deleteAll()
}