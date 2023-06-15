package pt.ulisboa.tecnico.cmov.librarist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import pt.ulisboa.tecnico.cmov.librarist.model.Ratings

@Dao
interface RatingsDao {
    @Query("SELECT * FROM ratings_table WHERE barcode = :barcode")
    fun getRatingsByBarcode(barcode: String): List<Ratings>

    @Query("SELECT AVG(rating) FROM ratings_table WHERE barcode = :barcode")
    fun getRatingsByBarcodeAVG(barcode: String): Double

    @Query("SELECT * FROM ratings_table ")
    fun getRatings(): List<Ratings>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addRatings(rating: Ratings)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addRatings(ratings: List<Ratings>)

    @Update
    suspend fun updateRatings(rating: Ratings)
}

@Dao
interface MyRatingsDao {
    @Query("SELECT * FROM ratings_table WHERE barcode = :barcode")
    fun getRatingsByBarcode(barcode: String): Ratings

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addRatings(rating: Ratings)

    @Update
    suspend fun updateRatings(rating: Ratings)
}