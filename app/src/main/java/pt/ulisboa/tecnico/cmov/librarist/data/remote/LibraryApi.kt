package pt.ulisboa.tecnico.cmov.librarist.data.remote

import okhttp3.ResponseBody
import pt.ulisboa.tecnico.cmov.librarist.model.Book
import pt.ulisboa.tecnico.cmov.librarist.model.BookListResponse
import pt.ulisboa.tecnico.cmov.librarist.model.Library
import pt.ulisboa.tecnico.cmov.librarist.model.LibraryListResponse
import pt.ulisboa.tecnico.cmov.librarist.model.LibraryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface LibraryApi {
    //LIBRARY

    // All libraries
    @GET("libs")
    suspend fun getLibraries(): Response<LibraryListResponse>

    // Get one library
    @GET("get_library_by_id")
    suspend fun getLibraryDetail(
        @Query("id") id: String
    ): Response<LibraryResponse>

    // Add library
    @POST("libs")
    suspend fun addLibrary(
        @Body library: Library
    ): Response<ResponseBody>

    // Update library
    @PATCH("libs/edit")
    suspend fun updateLibrary(
        @Query("id") id: String,
        @Body library: Library
    ): Response<ResponseBody>

    //BOOK

    // Get one book
    @GET("get_book_by_barcode")
    suspend fun getBook(
        @Query("barcode") id: String
    ): Response<Book>

    // Add book
    @POST("books")
    suspend fun addBook(
        @Body book: Book
    ): Response<ResponseBody>

    // Book search
    @GET("books_in_library/{id}")
    suspend fun getBooksInLibrary(@Path("id") id: String): Response<BookListResponse>
}