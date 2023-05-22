package pt.ulisboa.tecnico.cmov.librarist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pt.ulisboa.tecnico.cmov.librarist.model.book.Book
import pt.ulisboa.tecnico.cmov.librarist.model.library.Library

@Dao
interface LibraryDao {

    @Query("SELECT * FROM library_table")
    fun getLibraries(): List<Library>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addLibraries(libraries: List<Library>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(library: Library)

    @Update
    suspend fun updateLibrary(library: Library)

    @Query("SELECT * FROM library_table WHERE id = :id")
    fun getLibraryDetail(id: Int): Flow<Library>

    @Query("SELECT * FROM library_table WHERE name = :name")
    fun findLibrary(name: String): List<Library>

    @Query("DELETE FROM library_table WHERE id = :id")
    suspend fun deleteLibrary(id: Int)
}