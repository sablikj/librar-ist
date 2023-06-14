package pt.ulisboa.tecnico.cmov.librarist.utils

import androidx.room.TypeConverter


class StringListConverter {
    @TypeConverter
    fun fromStringList(vals: MutableList<String>?): String? {
        return vals?.joinToString(",")
    }

    @TypeConverter
    fun toStringList(string: String?): MutableList<String>? {
        return string?.split(",")?.toMutableList()
    }
}