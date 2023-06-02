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
    @SerialName("title")
    val name: String = "",
    val author: String = "",
    val notifications: Boolean = false,
    @SerialName("libraryId")
    var libraryId: String="",
    @SerialName("available") @Serializable(with = IntToBooleanSerializer::class)
    var available: Boolean = true,
    @SerialName("photo") @Serializable(with = ByteArrayBase64Serializer::class)
    val image: ByteArray = byteArrayOf()
)

@Serializable
data class BookListResponse(
    @SerialName("data")
    var data: List<Book>
)

@Serializable
data class BookResponse(var data: List<Book>)
