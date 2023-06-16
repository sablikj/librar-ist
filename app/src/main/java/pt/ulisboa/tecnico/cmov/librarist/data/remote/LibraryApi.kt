package pt.ulisboa.tecnico.cmov.librarist.data.remote

import okhttp3.ResponseBody
import pt.ulisboa.tecnico.cmov.librarist.model.Book
import pt.ulisboa.tecnico.cmov.librarist.model.BookLibResponse
import pt.ulisboa.tecnico.cmov.librarist.model.BookListResponse
import pt.ulisboa.tecnico.cmov.librarist.model.BookResponse
import pt.ulisboa.tecnico.cmov.librarist.model.CheckInBook
import pt.ulisboa.tecnico.cmov.librarist.model.Library
import pt.ulisboa.tecnico.cmov.librarist.model.LibraryListResponse
import pt.ulisboa.tecnico.cmov.librarist.model.LibraryResponse
import pt.ulisboa.tecnico.cmov.librarist.model.Ratings
import pt.ulisboa.tecnico.cmov.librarist.model.RatingsListAVGResponse
import pt.ulisboa.tecnico.cmov.librarist.model.RatingsListResponse
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
    suspend fun getLibraries(): Response<LibraryListResponse>

    // All libraries metered
    @GET("/libs/metered")
    suspend fun getLibrariesMetered(): Response<LibraryListResponse>

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

    // Get one book metered
    @GET("get_book_by_barcode_metered")
    suspend fun getBookMetered(
        @Query("barcode") id: String
    ): Response<BookResponse>

    // Get libraries where book is available
    @GET("get_libraries_by_book_title")
    suspend fun getBookLibraries(
        @Query("title") title: String
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
    @GET("search")
    suspend fun searchBooks(
        @Query("search") query: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): BookListResponse

    // search Book metered
    @GET("search_metered")
    suspend fun searchBooksMetered(
        @Query("search") query: String,
        @Query("offset") offset: Int,
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
        @Query("id") id: String
    ): Response<ResponseBody>

    // For display in library detail
    @GET("available_books_in_library/{id}")
    suspend fun getAvailableBooksInLibrary(
        @Path("id") id: String
    ): Response<BookListResponse>

    // For display in library detail - metered
    @GET("available_books_in_library_metered/{id}")
    suspend fun getAvailableBooksInLibraryMetered(
        @Path("id") id: String
    ): Response<BookListResponse>

    @GET("ratings")
    suspend fun getRatings(
    ): Response<RatingsListResponse>

    @POST("ratings")
    suspend fun addRating(
        @Body ratings: Ratings
    ): Response<ResponseBody>

    @POST("update_ratings")
    suspend fun updateRating(
        @Body ratings: Ratings
    ): Response<ResponseBody>

    @GET("/ratings_by_barcode")
    suspend fun getRatingsByBarcode(
        @Query("barcode") barcode: String
    ): Response<RatingsListResponse>

    @GET("/ratings_by_barcode_sum")
    suspend fun getRatingsByBarcodeAVG(
        @Query("barcode") barcode: String
    ): Response<RatingsListAVGResponse>

}