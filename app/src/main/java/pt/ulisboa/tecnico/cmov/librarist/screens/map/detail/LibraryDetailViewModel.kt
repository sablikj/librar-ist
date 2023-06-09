package pt.ulisboa.tecnico.cmov.librarist.screens.map.detail

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.ParcelFileDescriptor
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
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class LibraryDetailViewModel @Inject constructor(
    val repository: Repository,
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
    }

    // Bar code scanner
    private val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_EAN_13
        )
        .build()

    init {
        /*
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
        }*/
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
                // Task completed successfully
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
            // TODO: fix to local only
            repository.addLibrary(libraryDetail)
        }
    }

    // Converting URI to byteArray
    // TODO: add to separate file to avoid code duplicity
    fun uriToImage(uri: Uri) = viewModelScope.launch {
        val byteArray = uriToByteArray(uri)
        currentImageBytes.postValue(byteArray)
    }

    private suspend fun uriToByteArray(uri: Uri): ByteArray {
        return withContext(Dispatchers.IO) {
            var parcelFileDescriptor: ParcelFileDescriptor? = null
            try {
                parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
                val fileDescriptor = parcelFileDescriptor?.fileDescriptor
                val bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)

                // Read the orientation from the Exif data
                val exif = fileDescriptor?.let { ExifInterface(it) }
                val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

                // Create a matrix to perform transformations on the bitmap
                val matrix = Matrix()

                // Rotate the bitmap according to the orientation
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                }

                // Create a new bitmap that has been rotated correctly
                val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                // Resizing the image
                val targetWidth = 800  // specify desired width
                val scaleFactor = targetWidth.toDouble() / rotatedBitmap.width.toDouble()
                val targetHeight = (rotatedBitmap.height * scaleFactor).toInt()
                val resizedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, targetWidth, targetHeight, true)

                // Compressing the image and converting to ByteArray
                val outputStream = ByteArrayOutputStream()
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)

                return@withContext outputStream.toByteArray()
            } finally {
                // Ensure the ParcelFileDescriptor is closed
                parcelFileDescriptor?.close()
            }
        }
    }

    fun addNewBook(book: Book){
        // Adding book to the library
        val newBooks = libraryDetail.books.toMutableList()
        newBooks.add(book.barcode)
        libraryDetail.books = newBooks

        // Saving book to db and calling check-in
        viewModelScope.launch {
            repository.addBook(book)
            //repository.updateLibrary(libraryDetail)
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