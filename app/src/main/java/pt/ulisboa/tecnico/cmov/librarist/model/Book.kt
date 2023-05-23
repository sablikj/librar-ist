package pt.ulisboa.tecnico.cmov.librarist.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants.BOOK_TABLE

@Entity(tableName = BOOK_TABLE)
data class Book(
    @PrimaryKey(autoGenerate = false)
    val barcode: String = "",
    val name: String = "",
    val author: String = "",
    val available: Boolean = true,
    val image: ByteArray = byteArrayOf()
)