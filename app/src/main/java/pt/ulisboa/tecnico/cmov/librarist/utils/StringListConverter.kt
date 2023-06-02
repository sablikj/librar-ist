package pt.ulisboa.tecnico.cmov.librarist.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


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