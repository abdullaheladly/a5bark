package com.abdullah996.a5bark.db

import androidx.room.TypeConverter
import com.abdullah996.a5bark.model.Source


class Converters {
    @TypeConverter
    fun fromSource(source: Source):String{
        return source.name
    }

    @TypeConverter
    fun toSource(name:String): Source {
        return Source(name,name )
    }
}