package pt.ulisboa.tecnico.cmov.librarist.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pt.ulisboa.tecnico.cmov.librarist.utils.ByteArrayBase64Serializer
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants.BOOK_TABLE
import pt.ulisboa.tecnico.cmov.librarist.utils.IntToBooleanSerializer

@Serializable
@Entity(tableName = BOOK_TABLE)
data class Book(
    @PrimaryKey(autoGenerate = false)
    val barcode: String = "",
    val id: String = "", // for check-in, check-out
    @SerialName("title")
    val name: String = "",
    val author: String = "",
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
    var data: List<Book>
)
@Serializable
data class CheckInBook(
    @PrimaryKey(autoGenerate = false) @SerialName("bookCode")
    val barcode: String = "",
    val libraryId: String = "",
    @SerialName("id")
    val tableId: String = ""
)

@Serializable
data class BookLib(
    @PrimaryKey(autoGenerate = false)
    @SerialName("id")
    val tableId: String = "",
    @SerialName("bookCode")
    val barcode: String = "",
    val libraryId: String = "",
    @Serializable(with = IntToBooleanSerializer::class)
    val available: Boolean = true
)

@Serializable
data class BookLibResponse(
    @SerialName("data")
    var data: List<BookLib>
)