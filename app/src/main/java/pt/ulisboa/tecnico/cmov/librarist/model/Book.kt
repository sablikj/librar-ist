package pt.ulisboa.tecnico.cmov.librarist.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pt.ulisboa.tecnico.cmov.librarist.utils.ByteArrayBase64Serializer
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants.BOOK_TABLE

@Serializable
@Entity(tableName = BOOK_TABLE)
data class Book(
    @PrimaryKey(autoGenerate = false)
    val barcode: String = "",
    val id: String = "", // for check-in, check-out
    @SerialName("title")
    val name: String = "",
    val author: String = "",
    val notifications: Boolean = false,
    //@SerialName("libraryId")
    //var libraryId: String="",
    @SerialName("photo") @Serializable(with = ByteArrayBase64Serializer::class)
    val image: ByteArray = byteArrayOf()
)

@Serializable
data class BookListResponse(
    @SerialName("data")
    var data: List<Book>
)

@Serializable
data class BookResponse(
    @SerialName("data")
    var data: Book
)

data class CheckInBook(
    @PrimaryKey(autoGenerate = false) @SerialName("bookCode")
    val barcode: String = "",
    val libraryId: String = "",
    @SerialName("id")
    val tableId: String = ""
)
