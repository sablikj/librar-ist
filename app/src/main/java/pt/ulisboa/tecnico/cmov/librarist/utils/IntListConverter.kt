package pt.ulisboa.tecnico.cmov.librarist.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class IntListConverter {
    @TypeConverter
    fun fromIntList(vals: List<Int>?): String? {
        return vals?.joinToString(",")
    }

    @TypeConverter
    fun toIntList(intString: String?): List<Int>? {
        return intString?.split(",")?.map { it.toInt() }
    }
}

/*
class IntListConverter {
    @TypeConverter
    fun fromIntList(vals: List<Int>?): String? {
        if (vals == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.toJson(vals, type)
    }

    @TypeConverter
    fun toIntList(intString: String?): List<Int>? {
        if (intString == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(intString, type)
    }
}
*/