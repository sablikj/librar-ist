package pt.ulisboa.tecnico.cmov.librarist.data

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import pt.ulisboa.tecnico.cmov.librarist.data.local.LibraryDatabase
import pt.ulisboa.tecnico.cmov.librarist.data.paging.BookPagingSource
import pt.ulisboa.tecnico.cmov.librarist.data.remote.LibraryApi
import pt.ulisboa.tecnico.cmov.librarist.model.Book
import pt.ulisboa.tecnico.cmov.librarist.model.BookLib
import pt.ulisboa.tecnico.cmov.librarist.model.CheckInBook
import pt.ulisboa.tecnico.cmov.librarist.model.Library
import pt.ulisboa.tecnico.cmov.librarist.model.Notifications
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants.ITEMS_PER_PAGE
import javax.inject.Inject

class Repository @Inject constructor(
    private val libraryApi: LibraryApi,
    private val libraryDatabase: LibraryDatabase
) {
    private val libraryDao = libraryDatabase.libraryDao()
    private val bookDao = libraryDatabase.bookDao()
    private val notificationsDao = libraryDatabase.notificationsDao()

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
                libraryId = library.id)
            libraryApi.checkIn(checkBook)
        } catch (e: Exception) {
            Log.e("Repository", "Error adding book to the server", e)
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

    suspend fun getNotificationsForBook(barcode: String): Notifications? {
        try {
            val response = notificationsDao.getNotificationsForBook(barcode)
            return response
        } catch (e: Exception) {
            Log.d("getNotifications", "Error during GET: $e")
        }
        return null
    }

    suspend fun getNotifications(): List<Notifications> {
        try {
            val response = notificationsDao.getNotifications()
            return response
        } catch (e: Exception) {
            Log.d("getNotifications", "Error during GET: $e")
        }
        return emptyList()
    }

    suspend fun addNotifications(notifications: Notifications) {
        try {
            notificationsDao.addNotification(notifications)

        } catch (e: Exception) {
            Log.d("addNotifications", "Error during GET: $e")
        }
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

    suspend fun updateLibrary(library: Library){
        libraryDao.updateLibrary(library)

        // Try to update it on the server
        try { //TODO: fix
            //libraryApi.updateLibrary(library.id, library)
        } catch (e: Exception) {
            Log.e("Repository", "Error adding library to server", e)
        }
    }

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
                    return response.body()!!.data[0]
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

    // Search books
    fun searchBooks(query: String): Flow<PagingData<Book>> {
        return Pager(
            config = PagingConfig(pageSize = ITEMS_PER_PAGE),
            pagingSourceFactory = {
                BookPagingSource(libraryApi = libraryApi, query = query)
            }
        ).flow
    }

    suspend fun getBookLib(): List<BookLib>? {
        try {
            val response = libraryApi.getBookLib()
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
                bookDao.insert(response.body()!!.data[0])
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

    suspend fun getBookLibraries(title: String): List<Library>?{
        try {
            val response = libraryApi.getBookLibraries("\"$title\"") // client-side fix for SQL error

            if(response.isSuccessful && response.body() != null){
                val libraryIds = response.body()!!.data

                // Getting libraries
                var libraries = mutableListOf<Library>()
                for (lib in libraryIds){
                    try {
                        val libResponse = libraryApi.getLibraryDetail(lib.libraryId)
                        if(libResponse.isSuccessful && libResponse.body() !== null){
                            libraries.add(libResponse.body()!!.data)
                        }
                    }catch (e: Exception){
                        Log.d("Error while getting libraries for book", e.toString())
                    }
                }
                return libraries
            }
            return emptyList()

        }catch (e: Exception){
            Log.d("Error while getting libraries for book", e.toString())
        }
        return emptyList()
    }
}