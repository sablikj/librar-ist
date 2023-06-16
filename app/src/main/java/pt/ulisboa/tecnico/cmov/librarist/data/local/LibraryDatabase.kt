package pt.ulisboa.tecnico.cmov.librarist.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import pt.ulisboa.tecnico.cmov.librarist.data.local.dao.BookDao
import pt.ulisboa.tecnico.cmov.librarist.data.local.dao.LibraryDao
import pt.ulisboa.tecnico.cmov.librarist.data.local.dao.MyRatingsDao
import pt.ulisboa.tecnico.cmov.librarist.data.local.dao.NotificationsDao
import pt.ulisboa.tecnico.cmov.librarist.data.local.dao.RatingsDao
import pt.ulisboa.tecnico.cmov.librarist.model.Book
import pt.ulisboa.tecnico.cmov.librarist.model.Library
import pt.ulisboa.tecnico.cmov.librarist.model.MyRatings
import pt.ulisboa.tecnico.cmov.librarist.model.Notifications
import pt.ulisboa.tecnico.cmov.librarist.model.Ratings
import pt.ulisboa.tecnico.cmov.librarist.utils.LatLngConverter
import pt.ulisboa.tecnico.cmov.librarist.utils.StringListConverter
import pt.ulisboa.tecnico.cmov.librarist.utils.UUIDConverter


@TypeConverters(LatLngConverter::class, StringListConverter::class, UUIDConverter::class)
@Database(
    entities = [
        Book::class,
        Library::class,
        Notifications::class,
        Ratings::class,
        MyRatings::class
    ],
    version = 2,/*
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]*/)
abstract class LibraryDatabase: RoomDatabase() {
    // Objects
    abstract fun libraryDao(): LibraryDao
    abstract fun bookDao(): BookDao
    abstract fun notificationsDao(): NotificationsDao
    abstract fun ratingsDao(): RatingsDao
    abstract fun myRatingsDao(): MyRatingsDao
}