package com.amazonadonna.database

import android.arch.persistence.room.TypeConverter

class PictureListTypeConverter {
    companion object {
        const val NUM_PICS = 6
    }

    @TypeConverter
    fun fromArray(strings: Array<String>) : String? {
        var string = ""
        for(s in strings) string += (s + ",")

        return string
    }

    @TypeConverter
    fun toArray(concatenatedStrings: String) : Array<String>? {
        var myStrings: Array<String> = Array(NUM_PICS, { i -> "Not set"})

        for(s in concatenatedStrings.split(",")) {
            myStrings.plus(s)
        }

            return myStrings
    }
}