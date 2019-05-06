package com.amazonadonna.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.beust.klaxon.Json
import java.io.Serializable
import java.util.*

@Entity(tableName = "payout")
class Payout (
    @PrimaryKey @Json(name = "payoutId") var payoutId : String,
    @ColumnInfo(name = "amount") @Json(name = "price") var amount : Double,
    @ColumnInfo(name = "date") @Json(name = "date") var date : Long,
    @ColumnInfo(name = "artisanId") @Json(name = "price") var artisanId : String,
    @ColumnInfo(name = "synced") var synced : Int,
    @ColumnInfo(name = "signaturePicURL") var signaturePicURL : String,
    @ColumnInfo(name = "cgaId") @Json(name = "price") var cgaId : String) : Serializable {

    fun generateTempID() {
        var num = Random().nextInt()
        payoutId = artisanId + cgaId + num.toString()
    }

}