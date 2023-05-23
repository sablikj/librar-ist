package pt.ulisboa.tecnico.cmov.librarist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import pt.ulisboa.tecnico.cmov.librarist.model.Book
import pt.ulisboa.tecnico.cmov.librarist.model.Library


@Dao
interface BookDao {

    @Query("SELECT * FROM book_table")
    fun getBooks(): List<Book>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBooks(books: List<Book>)

    @Update
    suspend fun updateBook(book: Book)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(book: Book)

    @Query("SELECT * FROM book_table WHERE barcode = :id")
    fun getBookDetail(id: String): Book?

    @Query("SELECT * FROM book_table WHERE name = :name")
    fun findBook(name: String): List<Book>

    @Query("DELETE FROM book_table WHERE barcode = :id")
    suspend fun deleteBook(id: String)
}