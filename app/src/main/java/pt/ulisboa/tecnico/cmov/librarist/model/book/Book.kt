package pt.ulisboa.tecnico.cmov.librarist.model.book

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants.BOOK_TABLE

@Serializable
@Entity(tableName = BOOK_TABLE)
data class Book(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 0,
    val name: String = "",
    val author: String = "",
    val description: String = "",
    val image_url: String = "",
    val available: Boolean = true,
    val image: String = "app/src/main/res/drawable/ic_placeholder.xml"
)