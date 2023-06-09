package pt.ulisboa.tecnico.cmov.librarist.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pt.ulisboa.tecnico.cmov.librarist.utils.ByteArrayBase64Serializer
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants.LIBRARY_TABLE
import pt.ulisboa.tecnico.cmov.librarist.utils.LatLngConverter
import pt.ulisboa.tecnico.cmov.librarist.utils.LatLngSerializer
import pt.ulisboa.tecnico.cmov.librarist.utils.StringListConverter
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

    @TypeConverters(StringListConverter::class)
    var books: MutableList<String> = mutableListOf(), // All books IDs in the library (available or not)
    var favourite: Boolean = false, // Local storage only
    //@Ignore
    var distance: Int = 0
)
@Serializable
data class LibraryListResponse(var data: List<Library>)
@Serializable
data class LibraryResponse(var data: Library)
//TODO: prompt for permissions when checking in/out books

@Serializable
data class BooksInLibrary(
    val bookCode: String,
    val id: String,
    val libraryId: String
)

@Serializable
data class BooksInLibraryResponse(var data: List<BooksInLibrary>)