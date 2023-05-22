package pt.ulisboa.tecnico.cmov.librarist.model.library

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.android.gms.maps.model.LatLng
import pt.ulisboa.tecnico.cmov.librarist.model.book.Book
import pt.ulisboa.tecnico.cmov.librarist.utils.BookListConverter
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants.LIBRARY_TABLE
import pt.ulisboa.tecnico.cmov.librarist.utils.LatLngConverter

@Entity(tableName = LIBRARY_TABLE)
data class Library(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val image: ByteArray = byteArrayOf(),

    @TypeConverters(LatLngConverter::class)
    val location: LatLng = LatLng(0.0, 0.0),

    @TypeConverters(BookListConverter::class)
    val books: List<Book> = listOf() // All books in the library (available or not)
)