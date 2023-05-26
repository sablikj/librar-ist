package pt.ulisboa.tecnico.cmov.librarist.utils

import androidx.room.TypeConverter
import java.util.UUID

class UUIDConverter {
    @TypeConverter
    fun fromUUID(uuid: UUID): String {
        return uuid.toString()
    }

    @TypeConverter
    fun toUUID(str: String): UUID {
        return UUID.fromString(str)
    }
}