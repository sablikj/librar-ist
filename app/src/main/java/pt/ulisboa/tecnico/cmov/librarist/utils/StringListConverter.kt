package pt.ulisboa.tecnico.cmov.librarist.utils

import androidx.room.TypeConverter


class StringListConverter {
    @TypeConverter
    fun fromStringList(vals: List<String>?): String? {
        return vals?.joinToString(",")
    }

    @TypeConverter
    fun toStringList(string: String?): List<String>? {
        return string?.split(",")
    }
}