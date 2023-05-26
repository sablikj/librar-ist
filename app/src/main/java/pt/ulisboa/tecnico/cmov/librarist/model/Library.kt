package pt.ulisboa.tecnico.cmov.librarist.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pt.ulisboa.tecnico.cmov.librarist.utils.BookListConverter
import pt.ulisboa.tecnico.cmov.librarist.utils.ByteArrayBase64Serializer
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants.LIBRARY_TABLE
import pt.ulisboa.tecnico.cmov.librarist.utils.LatLngConverter
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
@Serializable
data class LibraryListResponse(var data: List<Library>)
@Serializable
data class LibraryResponse(var data: Library)
//TODO: prompt for permissions when checking in/out books

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