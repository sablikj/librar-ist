package pt.ulisboa.tecnico.cmov.librarist.model.library

import androidx.compose.ui.tooling.preview.Preview
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants.LIBRARY_TABLE

@Serializable
@Entity(tableName = LIBRARY_TABLE)
data class Library(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 0,
    val name: String? = "",
    val image_url: String = "",
    val location: String = "", // Coordinates
    val books: MutableList<Int> // IDs of all books in the library (available or not)
)