package com.amazonadonna.database

import androidx.room.TypeConverter
import android.util.Log

class PictureListTypeConverter {
    companion object {
        const val NUM_PICS = 6
    }

    @TypeConverter
    fun fromArray(strings: Array<String>) : String {
        var string = ""
        for(s in strings) string += (s + ",")
        string = string.substring(0, string.length - 1)

        return string
    }

    @TypeConverter
    fun toArray(concatenatedStrings: String) : Array<String> {
        var myStrings: Array<String> = Array(NUM_PICS, { i -> "undefined"})
        Log.i("PictureConversion", concatenatedStrings)

        var i = 0
        for(s in concatenatedStrings.split(",")) {
            myStrings[i++] = s
        }

        return myStrings
    }
}