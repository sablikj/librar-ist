package pt.ulisboa.tecnico.cmov.librarist.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pt.ulisboa.tecnico.cmov.librarist.model.Book
import pt.ulisboa.tecnico.cmov.librarist.utils.BookListConverter
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants.LIBRARY_TABLE
import pt.ulisboa.tecnico.cmov.librarist.utils.LatLngConverter
import pt.ulisboa.tecnico.cmov.librarist.utils.UUIDConverter
import java.util.Base64
import java.util.UUID
@Serializable
@Entity(tableName = LIBRARY_TABLE)
data class Library(
    @PrimaryKey(autoGenerate = false)
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    @SerialName("photo") @Serializable(with = ByteArrayBase64Serializer::class)
    var image: ByteArray = byteArrayOf(),
    @TypeConverters(LatLngConverter::class) @Serializable(with = LatLngSerializer::class)
    var location: LatLng = LatLng(0.0, 0.0),

    @TypeConverters(BookListConverter::class)
    var books: MutableList<Book> = mutableListOf(), // All books in the library (available or not)
    var favourite: Boolean = false // Local storage only
)

object LatLngSerializer : KSerializer<LatLng> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("location", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LatLng) {
        encoder.encodeString("${value.latitude},${value.longitude}")
    }

    override fun deserialize(decoder: Decoder): LatLng {
        val (lat, lon) = decoder.decodeString().split(',').map { it.toDouble() }
        return LatLng(lat, lon)
    }
}


object ByteArrayBase64Serializer: KSerializer<ByteArray> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("photo", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: ByteArray) {
        val base64String = Base64.getEncoder().encodeToString(value)
        encoder.encodeString(base64String)
    }

    override fun deserialize(decoder: Decoder): ByteArray {
        val base64String = decoder.decodeString()
        return Base64.getDecoder().decode(base64String)
    }
}