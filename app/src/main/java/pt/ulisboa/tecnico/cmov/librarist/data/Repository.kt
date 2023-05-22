package pt.ulisboa.tecnico.cmov.librarist.data

import android.util.Log
import pt.ulisboa.tecnico.cmov.librarist.data.local.LibraryDatabase
import pt.ulisboa.tecnico.cmov.librarist.data.remote.LibraryApi
import pt.ulisboa.tecnico.cmov.librarist.model.library.Library
import javax.inject.Inject

class Repository @Inject constructor(
    private val libraryApi: LibraryApi,
    private val libraryDatabase: LibraryDatabase
) {
    private val libraryDao = libraryDatabase.libraryDao()

    suspend fun addLibrary(library: Library) {
        libraryDao.insert(library)

        // Try to update it on the server
        try {
            libraryApi.addLibrary(library)
        } catch (e: Exception) {
            Log.e("Repository", "Error adding library to server", e)
        }
    }

    suspend fun getLibraries(): List<Library> {
        return try {
            val response = libraryApi.getLibraries()
            if(response.isSuccessful && response.body() != null){
                // If the API call is successful, update the local database and return the libraries
                libraryDao.addLibraries(response.body()!!)
                response.body()!!
            } else {
                // If the API call fails, return the libraries from the local database
                libraryDao.getLibraries()
            }
        } catch (e: Exception) {
            // If the API call fails, return the libraries from the local database
            libraryDao.getLibraries()
        }
    }

    suspend fun updateLibrary(library: Library) {
        libraryDao.updateLibrary(library)

        // Try to update it on the server
        try {
            libraryApi.updateLibrary(library.id, library)
        } catch (e: Exception) {
            Log.e("Repository", "Error updating library on server", e)
        }
    }
}