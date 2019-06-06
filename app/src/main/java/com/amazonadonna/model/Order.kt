package com.amazonadonna.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.amazonadonna.sync.Synchronizer
import com.beust.klaxon.Json
import java.io.Serializable

@Entity(tableName = "order")
data class Order (
        @ColumnInfo(name = "numItems") @Json(name = "numItems") var numItems : Int,
        @ColumnInfo(name = "shippingAddress") @Json(name = "shippingAddress") var shippingAddress : String,
        @PrimaryKey @Json(name = "orderId") var orderId : String,
        @ColumnInfo(name = "fulfilledStatus") @Json(name = "fulfilledStatus") var fulfilledStatus : Boolean,
        @ColumnInfo(name = "totalCostDollars") @Json(name = "totalCostDollars") var totalCostDollars : Int,
        @ColumnInfo(name = "totalCostCents") @Json(name = "totalCostCents") var totalCostCents : Int,
        //TODO uncomment when backend route supports amOrderNumber, otherwise causes crash
        //@ColumnInfo(name = "amOrderNumber") @Json(name = "amOrderNumber") var amOrderNumber : String,
        @ColumnInfo(name = "products") @Json(name = "products") var products : MutableList<Product>,
        @ColumnInfo(name = "cgaId") @Json(name = "cgaId") var cgaId : String,
        @ColumnInfo(name = "synced") var synced : Int = Synchronizer.SYNCED)
        //TODO uncomment when backend route supports orderDate, otherwise causes crash
        //@ColumnInfo(name = "orderDate") @Json(name = "orderDate") var orderDate : String)
        : Serializable {
}