package com.amazonadonna.database

import androidx.room.*
import com.amazonadonna.model.Order

@Dao
interface OrderDao {
    @Query("SELECT * FROM `order`")
    fun getAll(): List<Order>

    @Query("SELECT * FROM `order` WHERE synced = (:syncState)")
    fun getAllBySyncState(syncState: Int): List<Order>

    @Query("SELECT * FROM `order` WHERE orderId IN (:orderIds)")
    fun loadAllByIds(orderIds: IntArray): List<Order>

    @Query("UPDATE `order` SET synced = (:syncState) WHERE orderId = (:orderId)")
    fun setSyncedState(orderId: String, syncState: Int)

    @Query("SELECT * FROM `order` WHERE orderId LIKE :id " +
            "LIMIT 1")
    fun findByID(id: String): Order

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(orders: List<Order>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(order: Order)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(order: Order)

    @Delete
    fun delete(order: Order)

    @Query("DELETE FROM `order`")
    fun deleteAll()
}