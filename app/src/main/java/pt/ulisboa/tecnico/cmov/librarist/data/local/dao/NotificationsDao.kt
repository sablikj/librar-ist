package pt.ulisboa.tecnico.cmov.librarist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import pt.ulisboa.tecnico.cmov.librarist.model.Notifications

@Dao
interface NotificationsDao {
    @Query("SELECT * FROM notifications_table WHERE barcode = :barcode")
    fun getNotificationsForBook(barcode: String): Notifications

    @Query("SELECT * FROM notifications_table ")
    fun getNotifications(): List<Notifications>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNotification(notification: Notifications)

    @Update
    suspend fun updateNotifications(notification: Notifications)
}