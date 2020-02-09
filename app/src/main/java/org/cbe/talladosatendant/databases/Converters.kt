package org.cbe.talladosatendant.databases

import androidx.room.TypeConverter
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class Converters {

    var df: DateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
    @TypeConverter
    fun fromTimestamp(value: String?): Date? {
        return if (value != null) {
            try {
                return df.parse(value)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            null
        } else {
            null
        }
    }

    @TypeConverter
    fun fromDate(value: Date?): String? {
        return if (value != null){
            try{
                return df.format(value)
            } catch (e: ParseException){
                e.printStackTrace()
            }
            null
        }else
        {
            null
        }
    }

    /*@TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }*/
}