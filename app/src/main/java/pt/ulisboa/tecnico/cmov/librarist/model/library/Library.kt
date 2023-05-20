package pt.ulisboa.tecnico.cmov.librarist.model.library

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.android.gms.maps.model.LatLng
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants.LIBRARY_TABLE
import pt.ulisboa.tecnico.cmov.librarist.utils.LatLngConverter

@Entity(tableName = LIBRARY_TABLE)
@TypeConverters(LatLngConverter::class)
data class Library(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val image: ByteArray = byteArrayOf(),
    val location: LatLng = LatLng(0.0, 0.0),
    val books: MutableList<Int> = mutableListOf() // IDs of all books in the library (available or not)
)