package pt.ulisboa.tecnico.cmov.librarist.screens.map.detail

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.ulisboa.tecnico.cmov.librarist.data.Repository
import pt.ulisboa.tecnico.cmov.librarist.model.Book
import pt.ulisboa.tecnico.cmov.librarist.model.Library
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants
import pt.ulisboa.tecnico.cmov.librarist.utils.ImageUtils
import javax.inject.Inject

@HiltViewModel
class LibraryDetailViewModel @Inject constructor(
    val repository: Repository,
    val imageUtils: ImageUtils,
    savedStateHandle: SavedStateHandle,
    private val contentResolver: ContentResolver
): ViewModel() {

    val scanResult = MutableLiveData<String>()
    val currentImageBytes: MutableLiveData<ByteArray> = MutableLiveData()
    val showBookDialog = mutableStateOf(false)
    val checkIn = mutableStateOf(false)
    val processBarCode = mutableStateOf(false)
    val loading = mutableStateOf(false)
    val libraryId = savedStateHandle.get<String>(Constants.Routes.LIBRARY_DETAIL_ID) ?: throw IllegalArgumentException("Library ID is missing")
    var libraryDetail by mutableStateOf(Library())
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books

    fun onBooksChanged(books: List<Book>) {
        _books.value = books
        libraryDetail = libraryDetail.copy(
            books = books.map { it.barcode } as MutableList<String>
        )
    }

    // Bar code scanner
    private val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_EAN_13
        )
        .build()

    init {
        initLibrary()
    }

    fun initLibrary(){
        libraryId.let {
            loading.value = true
            viewModelScope.launch(Dispatchers.IO) {
                repository.refreshLibraryDetail(libraryId)
                //get books for library
                val currentBooks = getBooksInLibrary(libraryId)
                onBooksChanged(currentBooks)
                // TODO: call getLibrary instead
                repository.getLibraryDetail(it).collect { detail ->
                    withContext(Dispatchers.Main) {
                        libraryDetail = detail
                        loading.value = false
                    }
                }
            }
        }
    }

    fun ProcessBook(context: Context, checkInBook: Boolean) {
        val scanner = GmsBarcodeScanning.getClient(context, options)
        scanner.startScan()
            .addOnSuccessListener { barcode ->
                scanResult.value = barcode.rawValue.toString()
                processBarCode.value = true
                checkIn.value = checkInBook
            }
            .addOnCanceledListener {
                Log.d("Barcode", "Barcode scanning was canceled")
            }
            .addOnFailureListener { e ->
                Log.d("Barcode", "Barcode scanning failed: $e")
            }
    }

    fun favourite(){
        libraryDetail = libraryDetail.copy(favourite = !libraryDetail.favourite)

        // Updating library locally
        viewModelScope.launch {
            repository.updateLibrary(libraryDetail)
        }
    }

    // Converting URI to byteArray
    fun uriToImage(uri: Uri) = viewModelScope.launch {
        val byteArray = imageUtils.uriToByteArray(contentResolver, uri)
        currentImageBytes.postValue(byteArray)
    }

    fun addNewBook(book: Book){
        // Adding book to the library
        val newBooks = libraryDetail.books.toMutableList()
        newBooks.add(book.barcode)
        libraryDetail.books = newBooks

        // Saving book to db and calling check-in
        viewModelScope.launch {
            repository.addBook(book)
            libraryDetail.books.add(book.barcode)
            repository.checkInBook(book, libraryDetail)
        }
    }

    //get books by Library
    @OptIn(DelicateCoroutinesApi::class)
    suspend fun getBooksInLibrary(id: String):List<Book> {
        var books= emptyList<Book>()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                books = repository.getAvailableBooksInLibraries(id)
                onBooksChanged(books)
            } catch (t: Throwable) {
                Log.d("ErrorLaunchDetail", t.toString())
            }
        }
        return books;
    }
}