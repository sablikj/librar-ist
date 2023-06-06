package pt.ulisboa.tecnico.cmov.librarist.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import pt.ulisboa.tecnico.cmov.librarist.data.local.LibraryDatabase
import pt.ulisboa.tecnico.cmov.librarist.data.remote.LibraryApi
import pt.ulisboa.tecnico.cmov.librarist.model.Book
import pt.ulisboa.tecnico.cmov.librarist.model.BookLib
import pt.ulisboa.tecnico.cmov.librarist.model.CheckInBook
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

    suspend fun checkInBook(book: Book, library: Library){
        // First update library locally
        libraryDao.updateLibrary(library)

        // Try to update it on the server
        try {
            val checkBook = CheckInBook(
                barcode = book.barcode,
                libraryId = library.id,
                tableId = book.id)
            libraryApi.checkIn(checkBook)
        } catch (e: Exception) {
            Log.e("Repository", "Error adding library to server", e)
        }
    }

    suspend fun getLibraries(): List<Library> {
        // get libraries from api
        try {
            val response = libraryApi.getLibraries()
            if(response.isSuccessful && response.body() != null){
                // If the API call is successful, update the local database and return the libraries
                val libraries = response.body()?.data ?: emptyList()
                libraryDao.addLibraries(libraries)
                return libraries
            } else {
                return emptyList()
            }
        } catch (e: Exception) {
            Log.d("getLibraries", "Error during GET: $e")
        }
        val localLibraries = libraryDao.getLibraries()

        // If the local database is not empty, return the libraries from it
        if (localLibraries.isNotEmpty()) {
            return localLibraries
        }
        return localLibraries
    }

    suspend fun getAvailableBooksInLibraries(id: String): List<Book> {
        // get books from api
        try {
            val response = libraryApi.getAvailableBooksInLibrary(id)
            if (response.isSuccessful && response.body() != null) {
                // If the API call is successful, update the local database and return the libraries
                val books = response.body()?.data ?: emptyList()
                bookDao.addBooks(books)
                return books
            } else {
                return emptyList()
            }
        } catch (e: Exception) {
            Log.d("getBooks", "Error during GET: $e")
        }
        val localBooks = bookDao.getBooks()

        // If the local database is not empty, return the libraries from it
        if (localBooks.isNotEmpty()) {
            return localBooks
        }
        return localBooks
    }

    suspend fun refreshLibraryDetail(id: String) {
        try {
            val response = libraryApi.getLibraryDetail(id)
            if(response.isSuccessful && response.body() != null){
                // If the API call is successful, update the local database and return the libraries
                val library = response.body()?.data
                if (library != null) {
                    libraryDao.insert(library)
                }else{
                    Log.d("API", "Failed to fetch the data.")
                }
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
                    return response.body()!!.data
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

    suspend fun getBookLib(barcode: String): List<BookLib>? {
        try {
            val response = libraryApi.getBookLib(barcode)
            if (response.isSuccessful && response.body() != null) {
                // Book located on the server
                return response.body()!!.data
            } else {
                // Book does not exist in local or server data
                return null
            }
        } catch (e: Exception) {
            Log.d("ErrorLaunchDetail", e.toString())
        }
        return null
    }

    suspend fun refreshBookDetail(barcode: String) {
        try {
            val response = libraryApi.getBook(barcode)
            if(response.isSuccessful && response.body() != null){
                // If the API call is successful, update the local database and return the book
                bookDao.insert(response.body()!!.data)
                response.body()!!
            } else {
                Log.d("API", "Failed to fetch the data.")
            }
        }catch (e: Exception){
            Log.d("ErrorLaunchDetail", e.toString())
        }
    }

    fun getFavouriteLibraryIds(): List<String> {
        var result = mutableListOf<String>()
        try {
            val libs = libraryDao.getLibraries()
            for (l in libs) {
                if(l.favourite) {
                    result.add(l.id)
                }
            }
        } catch (e: Exception) {
            Log.d("Error in getting favourite libraries", e.toString())
        }
        return result
    }

    suspend fun saveBooksNotifications(barcode: String,notificationUpdate:Boolean) {
        try {
            var currentBook = getBook(barcode)?.let {
                bookDao.updateBook(
                    Book(
                        barcode = it.id,
                        name = it.name,
                        image = it.image,
                        author = it.author,
                        notifications = notificationUpdate,
                    )
                )
            }
        } catch (e: Exception) {
            Log.d("Error in saveBooksNotifications", e.toString())
        }
    }
}