package pt.ulisboa.tecnico.cmov.librarist.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import pt.ulisboa.tecnico.cmov.librarist.data.local.LibraryDatabase
import pt.ulisboa.tecnico.cmov.librarist.data.remote.LibraryApi
import pt.ulisboa.tecnico.cmov.librarist.model.Book
import pt.ulisboa.tecnico.cmov.librarist.model.Library
import javax.inject.Inject

class Repository @Inject constructor(
    private val libraryApi: LibraryApi,
    private val libraryDatabase: LibraryDatabase
) {
    private val libraryDao = libraryDatabase.libraryDao()
    private val bookDao = libraryDatabase.bookDao()

    suspend fun addLibrary(library: Library) {
        libraryDao.insert(library)

        // Try to update it on the server
        try {
            libraryApi.addLibrary(library)
        } catch (e: Exception) {
            Log.e("Repository", "Error adding library to server", e)
        }
    }

    suspend fun updateLibrary(library: Library){
        libraryDao.updateLibrary(library)

        // Try to update it on the server
        try {
            libraryApi.updateLibrary(library.id, library)
        } catch (e: Exception) {
            Log.e("Repository", "Error adding library to server", e)
        }
    }

    suspend fun getLibraries(): List<Library> {
        val localLibraries = libraryDao.getLibraries()

        // If the local database is not empty, return the libraries from it
        if (localLibraries.isNotEmpty()) {
            return localLibraries
        }

        // If the local database is empty, make the API call
        return try {
            val response = libraryApi.getLibraries()
            if(response.isSuccessful && response.body() != null){
                // If the API call is successful, update the local database and return the libraries
                libraryDao.addLibraries(response.body()!!)
                response.body()!!
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    suspend fun refreshLibraryDetail(id: String) {
        try {
            val response = libraryApi.getLibraryDetail(id)
            if(response.isSuccessful && response.body() != null){
                // If the API call is successful, update the local database and return the libraries
                libraryDao.insert(response.body()!!)
                response.body()!!
            } else {
                Log.d("API", "Failed to fetch the data.")
            }
        }catch (e: Exception){
            Log.d("ErrorLaunchDetail", e.toString())
        }
    }

    fun getLibraryDetail(id: String): Flow<Library> =
        libraryDatabase.libraryDao().getLibraryDetail(id)


    suspend fun addBook(book: Book) {
        bookDao.insert(book)

        // Try to update it on the server
        try {
            libraryApi.addBook(book)
        } catch (e: Exception) {
            Log.e("Repository", "Error adding book to server", e)
        }
    }

    // Mainly for book check-in
    suspend fun getBook(barcode: String): Book? {
        // Look for book in local data
        val localBook = bookDao.getBookDetail(barcode)

        // Book not found
        if (localBook == null){
            // Look for book in server
            try {
                val response = libraryApi.getBook(barcode)
                if(response.isSuccessful && response.body() != null){
                    // Book located on the server
                    return response.body()!!
                } else {
                    // Book does not exist in local or server data
                    return null
                }
            }catch (e: Exception){
                Log.d("ErrorLaunchDetail", e.toString())
            }
        }
        // Return book from local storage
        return localBook
    }

    suspend fun refreshBookDetail(barcode: String) {
        try {
            val response = libraryApi.getBook(barcode)
            if(response.isSuccessful && response.body() != null){
                // If the API call is successful, update the local database and return the book
                bookDao.insert(response.body()!!)
                response.body()!!
            } else {
                Log.d("API", "Failed to fetch the data.")
            }
        }catch (e: Exception){
            Log.d("ErrorLaunchDetail", e.toString())
        }
    }
}