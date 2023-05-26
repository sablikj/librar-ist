package pt.ulisboa.tecnico.cmov.librarist.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
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
import java.util.UUID
@Serializable
@Entity(tableName = LIBRARY_TABLE)
data class Library(
    @PrimaryKey(autoGenerate = false)
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var image: ByteArray = byteArrayOf(),
    @TypeConverters(LatLngConverter::class)
    @Serializable(with = LatLngSerializer::class)
    var location: LatLng = LatLng(0.0, 0.0),

    @TypeConverters(BookListConverter::class)
    var books: MutableList<Book> = mutableListOf(), // All books in the library (available or not)
    var favourite: Boolean = false // Local storage only
)

object LatLngSerializer : KSerializer<LatLng> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("LatLng") {
            element<Double>("latitude")
            element<Double>("longitude")
        }

    override fun serialize(encoder: Encoder, value: LatLng) {
        val composite = encoder.beginStructure(descriptor)
        composite.encodeDoubleElement(descriptor, 0, value.latitude)
        composite.encodeDoubleElement(descriptor, 1, value.longitude)
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): LatLng {
        val dec: CompositeDecoder = decoder.beginStructure(descriptor)
        var lat = 0.0
        var lon = 0.0

        loop@ while (true) {
            when (val index = dec.decodeElementIndex(descriptor)) {
                0 -> lat = dec.decodeDoubleElement(descriptor, 0)
                1 -> lon = dec.decodeDoubleElement(descriptor, 1)
                CompositeDecoder.DECODE_DONE -> break@loop
                else -> throw SerializationException("Unexpected index $index")
            }
        }

        return LatLng(lat, lon)
    }
}