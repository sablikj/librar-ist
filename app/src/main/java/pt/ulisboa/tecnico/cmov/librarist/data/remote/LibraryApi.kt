package pt.ulisboa.tecnico.cmov.librarist.data.remote

import okhttp3.ResponseBody
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
    //LIBRARY

    // All libraries
    @GET("libs")
    suspend fun getLibraries(): Response<List<Library>>

    // Get one library
    @GET("get_library_by_id/{id}/")
    suspend fun getLibraryDetail(
        @Path("id") id: String
    ): Response<Library>

    // Add library
    @POST("libs")
    suspend fun addLibrary(
        @Body library: Library
    ): Response<ResponseBody>

    // Update library
    @PUT("libs/edit/{id}")
    suspend fun updateLibrary(
        @Path("id") id: String,
        @Body library: Library
    ): Response<ResponseBody>

    //BOOK

    // Get one book
    @GET("get_book_by_barcode/{id}/")
    suspend fun getBook(
        @Path("id") id: String
    ): Response<Book>

    // Add book
    @POST("books")
    suspend fun addBook(
        @Body book: Book
    ): Response<ResponseBody>

    // Book search
    @GET("books_in_library/?") //TODO: update
    suspend fun searchBooks(
        @Query("search") query: String
    ): Response<List<Book>>
}