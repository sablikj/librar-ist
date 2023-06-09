package pt.ulisboa.tecnico.cmov.librarist.data.remote

import okhttp3.ResponseBody
import pt.ulisboa.tecnico.cmov.librarist.model.Book
import pt.ulisboa.tecnico.cmov.librarist.model.BookLibResponse
import pt.ulisboa.tecnico.cmov.librarist.model.BookListResponse
import pt.ulisboa.tecnico.cmov.librarist.model.BookResponse
import pt.ulisboa.tecnico.cmov.librarist.model.BooksInLibraryResponse
import pt.ulisboa.tecnico.cmov.librarist.model.CheckInBook
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

    //BOOK

    // Get one book
    @GET("get_book_by_barcode")
    suspend fun getBook(
        @Query("barcode") id: String
    ): Response<BookResponse>

    // Get books from BookLib
    @GET("get_booklib_by_barcode")
    suspend fun getBookLibByBarcode(
        @Query("barcode") id: String
    ): Response<BookLibResponse>

    @GET("get_book_lib")
    suspend fun getBookLib(
    ): Response<BookLibResponse>

    // Add book
    @POST("books")
    suspend fun addBook(
        @Body book: Book
    ): Response<ResponseBody>

    // search Book
    //TODO: add endpoint
    @GET("")
    suspend fun searchBooks(
        @Query("search") query: String,
        @Query("limit") limit: Int
    ): BookListResponse

    // Check-in book
    @POST("checkin")
    suspend fun checkIn(
        @Body book: CheckInBook
    ): Response<ResponseBody>

    // Check-out book
    @PUT("checkout")
    suspend fun checkOut(
        @Query("barcode") barcode: String,
        @Query("libraryId") libraryId: String,
        @Body book: Book
    ): Response<ResponseBody>

    // For display in library detail
    @GET("available_books_in_library/{id}")
    suspend fun getAvailableBooksInLibrary(
        @Path("id") id: String
    ): Response<BookListResponse>

    // Checking if exists
    @GET("books_in_library/{id}")
    suspend fun getBooksInLibrary(
        @Path("id") id: String
    ): Response<BooksInLibraryResponse>
}