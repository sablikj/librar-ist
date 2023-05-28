package pt.ulisboa.tecnico.cmov.librarist.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import pt.ulisboa.tecnico.cmov.librarist.data.local.dao.BookDao
import pt.ulisboa.tecnico.cmov.librarist.data.local.dao.LibraryDao
import pt.ulisboa.tecnico.cmov.librarist.model.Book
import pt.ulisboa.tecnico.cmov.librarist.model.Library
import pt.ulisboa.tecnico.cmov.librarist.utils.BookListConverter
import pt.ulisboa.tecnico.cmov.librarist.utils.IntListConverter
import pt.ulisboa.tecnico.cmov.librarist.utils.LatLngConverter
import pt.ulisboa.tecnico.cmov.librarist.utils.UUIDConverter


@TypeConverters(LatLngConverter::class, BookListConverter::class, IntListConverter::class, UUIDConverter::class)
@Database(
    entities = [
        Book::class,
        Library::class
    ],
    version = 2,/*
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]*/)
abstract class LibraryDatabase: RoomDatabase() {
    // Objects
    abstract fun libraryDao(): LibraryDao
    abstract fun bookDao(): BookDao
}