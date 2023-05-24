package pt.ulisboa.tecnico.cmov.librarist.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.android.gms.maps.model.LatLng
import pt.ulisboa.tecnico.cmov.librarist.model.Book
import pt.ulisboa.tecnico.cmov.librarist.utils.BookListConverter
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants.LIBRARY_TABLE
import pt.ulisboa.tecnico.cmov.librarist.utils.LatLngConverter

@Entity(tableName = LIBRARY_TABLE)
data class Library(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var name: String = "",
    var image: ByteArray = byteArrayOf(),
    @TypeConverters(LatLngConverter::class)
    var location: LatLng = LatLng(0.0, 0.0),

    @TypeConverters(BookListConverter::class)
    var books: MutableList<Book> = mutableListOf(), // All books in the library (available or not)
    var favourite: Boolean = false // Local storage only
)