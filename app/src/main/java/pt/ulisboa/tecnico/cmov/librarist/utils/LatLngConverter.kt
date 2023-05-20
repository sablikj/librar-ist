package pt.ulisboa.tecnico.cmov.librarist.utils

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng

class LatLngConverter {
    @TypeConverter
    fun fromLatLng(latLng: LatLng): String {
        return "${latLng.latitude},${latLng.longitude}"
    }

    @TypeConverter
    fun toLatLng(data: String): LatLng {
        val split = data.split(",")
        return LatLng(split[0].toDouble(), split[1].toDouble())
    }
}
