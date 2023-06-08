package pt.ulisboa.tecnico.cmov.librarist.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants

@Serializable
@Entity(tableName = Constants.NOTIFICATIONS_TABLE)
data class Notifications(
    @PrimaryKey(autoGenerate = false)
    val barcode: String,
    val notifications: Boolean
)