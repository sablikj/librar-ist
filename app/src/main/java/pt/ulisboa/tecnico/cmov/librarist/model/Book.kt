package pt.ulisboa.tecnico.cmov.librarist.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.serialization.Serializable
import pt.ulisboa.tecnico.cmov.librarist.utils.BookListConverter
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants.BOOK_TABLE
import pt.ulisboa.tecnico.cmov.librarist.utils.IntListConverter
@Serializable
@Entity(tableName = BOOK_TABLE)
data class Book(
    @PrimaryKey(autoGenerate = false)
    val barcode: String = "",
    val name: String = "",
    val author: String = "",
    val notifications: Boolean = false,

    @TypeConverters(IntListConverter::class)
    var libraries: MutableList<Int> = mutableListOf(),
    val image: ByteArray = byteArrayOf()
)