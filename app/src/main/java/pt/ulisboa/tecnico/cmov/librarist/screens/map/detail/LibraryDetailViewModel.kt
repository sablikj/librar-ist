package pt.ulisboa.tecnico.cmov.librarist.screens.map.detail

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.ulisboa.tecnico.cmov.librarist.BuildConfig
import pt.ulisboa.tecnico.cmov.librarist.R
import pt.ulisboa.tecnico.cmov.librarist.data.Repository
import pt.ulisboa.tecnico.cmov.librarist.model.Book
import pt.ulisboa.tecnico.cmov.librarist.model.Library
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants
import pt.ulisboa.tecnico.cmov.librarist.utils.ImageUtils
import pt.ulisboa.tecnico.cmov.librarist.utils.LocationUtils
import pt.ulisboa.tecnico.cmov.librarist.utils.isInternetAvailable
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class LibraryDetailViewModel @Inject constructor(
    val repository: Repository,
    val application: Application,
    private val imageUtils: ImageUtils,
    private val locationUtils: LocationUtils,
    savedStateHandle: SavedStateHandle,
    private val contentResolver: ContentResolver
): ViewModel() {

    val scanResult = MutableLiveData<String>()
    val currentImageBytes: MutableLiveData<ByteArray> = MutableLiveData()
    val showBookDialog = mutableStateOf(false)
    val checkIn = mutableStateOf(false)
    val processBarCode = mutableStateOf(false)
    val saveBook = mutableStateOf(true)
    val loading = mutableStateOf(false)
    val libraryId = savedStateHandle.get<String>(Constants.Routes.LIBRARY_DETAIL_ID) ?: throw IllegalArgumentException(application.getString(
        R.string.library_id_missing))
    var libraryDetail by mutableStateOf(Library())
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books

    suspend fun onBooksChanged(books: List<Book>) {
        _books.value = books
        Log.d("Books","Books ${books.size}")
        if(saveBook.value && books.isNotEmpty() && isInternetAvailable(application.applicationContext)){
            saveBooks(books)
            saveBook.value = false
        }

    }

    suspend fun saveBooks(books: List<Book>){
        // Saving books locally
        val newLibrary = libraryDetail.copy()
        Log.d("Books","Books copy")
        for(book in books){
            if(!newLibrary.books.contains(book.barcode) && book.barcode != ""){
                newLibrary.books.add(book.barcode)
                Log.d("Books","Books ${book.barcode}")
            }
        }
        Log.d("Books","Books repo")
        //Log.d("Books","Books $newLibrary")
        repository.updateLibrary(newLibrary)

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

    @OptIn(DelicateCoroutinesApi::class)
    fun initLibrary(){
        val isInternetAvailable = isInternetAvailable(application.applicationContext)
        libraryId.let {
            loading.value = true
            viewModelScope.launch(Dispatchers.IO) {
                if(isInternetAvailable){
                    repository.refreshLibraryDetail(libraryId)
                    //get books for library
                    val currentBooks = getBooksInLibrary(libraryId)
                }

                repository.getLibraryDetail(it).collect { detail ->
                    if(detail != null){
                        withContext(Dispatchers.Main) {
                            libraryDetail = detail
                            loading.value = false
                        }.let {
                            if(!isInternetAvailable){
                                if(_books.value.isEmpty()){
                                    Log.d("Books","Books local")
                                    val books = repository.getLocalBooks(libraryDetail.books)
                                    onBooksChanged(books)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun processBook(context: Context, checkInBook: Boolean) {
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
        var books = emptyList<Book>()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                books = repository.getAvailableBooksInLibraries(application.applicationContext, id)
                onBooksChanged(books)
            } catch (t: Throwable) {
                Log.d("ErrorLaunchDetail", t.toString())
            }
        }
        return books;
    }

    fun shareLibrary(context: Context, lifecycleScope: CoroutineScope) {
        // Launching in Lifecycle scope to prevent memory leaks on cancel
        lifecycleScope.launch(Dispatchers.IO) {
            val bitmap = BitmapFactory.decodeByteArray(libraryDetail.image, 0, libraryDetail.image.size)

            // Create a file in the cache directory to share
            val file = File(context.cacheDir, "${libraryDetail.name.replace(' ', '_')}.png")
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            // Get the Uri for the file using the FileProvider
            val fileUri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)

            // Library address
            val address = locationUtils.getReadableLocation(libraryDetail.location, context)
            // Switching to Main (UI) Thread to start the Activity
            withContext(Dispatchers.Main) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "${libraryDetail.name} ${R.string.share_library_location} $address")
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    type = "image/*"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "${R.string.share_library}"))
            }
        }
    }
}