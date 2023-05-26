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
    @GET("libs")
    suspend fun getLibraries(): Response<List<Library>>

    // Get one library
    @GET("get_library_by_id/{id}/")
    suspend fun getLibraryDetail(
        @Path("id") id: Int
    ): Response<Library>

    // Add library
    @POST("libs")
    suspend fun addLibrary(
        @Body library: Library
    ): Response<String>

    // Update library
    //TODO: Use PUT or PATCH? - based on API implementation
    @PUT("libs/edit/{id}")
    suspend fun updateLibrary(
        @Path("id") id: Int,
        @Body library: Library
    ): Response<String>

    //BOOK

    // Get one book
    @GET("books_in_library/{id}/")
    suspend fun getBook(
        @Path("id") id: String
    ): Response<Book>

    @GET("/get_book_by_title?title=")
    suspend fun getBookByTitle(
        @Query("title") title: String
    ): Response<Book>

    // Add book
    @POST("books")
    suspend fun addBook(
        @Body book: Book
    ): Response<String>

    // Book search
    @GET("books_in_library/?") //TODO: fix
    suspend fun searchBooks(
        @Query("search") query: String
    ): Response<List<Book>>
}