package pt.ulisboa.tecnico.cmov.librarist.data.remote

import pt.ulisboa.tecnico.cmov.librarist.model.Book
import pt.ulisboa.tecnico.cmov.librarist.model.Library
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface LibraryApi {
    //TODO: add api calls

    //LIBRARY

    // All libraries
    @GET("libraries")
    suspend fun getLibraries(): Response<List<Library>>

    // Get one library
    @GET("libraries/{id}/")
    suspend fun getLibraryDetail(
        @Path("id") id: Int
    ): Response<Library>

    // Add library
    @POST("libraries")
    suspend fun addLibrary(
        @Body library: Library
    ): Response<Library>

    // Update library
    //TODO: Use PUT or PATCH? - based on API implementation
    @PUT("libraries/{id}")
    suspend fun updateLibrary(
        @Path("id") id: Int,
        @Body library: Library
    ): Response<Library>

    //BOOK

    // Get one book
    @GET("books_in_library/{id}/")
    suspend fun getBook(
        @Path("id") id: String
    ): Response<Book>

    // Add book
    @POST("books_in_library")
    suspend fun addBook(
        @Body book: Book
    ): Response<Book>

    // Book search
    @GET("books_in_library/?")
    suspend fun searchBooks(
        @Query("search") query: String
    ): Response<List<Book>>
}