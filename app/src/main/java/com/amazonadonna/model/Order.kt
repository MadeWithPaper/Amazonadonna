package com.amazonadonna.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.ColumnInfo
import com.beust.klaxon.Json
import java.io.Serializable
import java.util.*

@Entity(tableName = "order")
data class Order (
        @ColumnInfo(name = "shippingAddress") @Json(name = "shippingAddress") var shippingAddress : String,
        @PrimaryKey @Json(name = "orderId") var orderId : String,
        @ColumnInfo(name = "totalCost") @Json(name = "totalCost") var totalCost : Double,
        @ColumnInfo(name = "products") @Json(name = "products") var items : List<Product>,
        @ColumnInfo(name = "cgaId") @Json(name = "cgaId") var cgaId : String): Serializable {

    fun generateOrderID() {
        //TODO fill in logic for generating unique ID for artisan
        var num = Random().nextInt()
        orderId = shippingAddress + cgaId + num.toString()
    }



}