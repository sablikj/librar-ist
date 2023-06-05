package pt.ulisboa.tecnico.cmov.librarist.utils

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

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

object LatLngSerializer : KSerializer<LatLng> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("location", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LatLng) {
        encoder.encodeString("${value.latitude},${value.longitude}")
    }

    override fun deserialize(decoder: Decoder): LatLng {
        val latLngAsString = decoder.decodeString()
        val (latitude, longitude) = latLngAsString.split(",").map { it.toDouble() }
        return LatLng(latitude, longitude)
    }
}