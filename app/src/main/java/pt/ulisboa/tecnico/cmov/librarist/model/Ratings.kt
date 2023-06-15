package pt.ulisboa.tecnico.cmov.librarist.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants

@Serializable
@Entity(tableName = Constants.RATINGS_TABLE)
data class Ratings(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val barcode: String,
    val rating: Int
)

@Serializable
@Entity(tableName = Constants.MY_RATINGS_TABLE)
data class MyRatings(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val barcode: String,
    val rating: Int
)

@Serializable
data class AVGRating(
    @SerialName("AVG(Ratings.rating)")
    val avgRating: Double
)

@Serializable
data class RatingsListResponse(var data: List<Ratings>)

@Serializable
data class RatingsListAVGResponse(var data: List<AVGRating>)
