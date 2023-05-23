package pt.ulisboa.tecnico.cmov.librarist.screens.map.detail

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
    val processBarCode = mutableStateOf(false)

    val loading = mutableStateOf(false)
    val libraryId = savedStateHandle.get<Int>(Constants.Routes.LIBRARY_DETAIL_ID) ?: throw IllegalArgumentException("Library ID is missing")

    var libraryDetail by mutableStateOf(Library())

    // Bar code scanner
    private val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_EAN_13
        )
        .build()

    init {
        libraryId.let {
            loading.value = true
            viewModelScope.launch(Dispatchers.IO) {
                repository.refreshLibraryDetail(libraryId)
                repository.getLibraryDetail(it).collect {detail ->
                    withContext(Dispatchers.Main) {
                        libraryDetail = detail
                        loading.value = false
                    }
                }
            }
        }
    }

    fun checkIn(context: Context) {
        val scanner = GmsBarcodeScanning.getClient(context, options)
        scanner.startScan()
            .addOnSuccessListener { barcode ->
                // Task completed successfully
                scanResult.value = barcode.rawValue.toString()
                processBarCode.value = true
            }
            .addOnCanceledListener {
                Log.d("Barcode", "Barcode scanning was canceled")
            }
            .addOnFailureListener { e ->
                Log.d("Barcode", "Barcode scanning failed: $e")
            }
    }

    fun checkOut(){
        //TODO: Implement
        // Should open the camera and scan the barcode, then remove from library
    }

    fun favourite(){
        libraryDetail = libraryDetail.copy(favourite = !libraryDetail.favourite)

        // Updating library locally
        viewModelScope.launch {
            repository.addLibrary(libraryDetail)
        }
    }

    // Converting URI to byteArray
    //TODO: remove duplicate code (these fncs and same ones in the MapViewModel)
    fun uriToImage(uri: Uri) = viewModelScope.launch {
        val byteArray = uriToByteArray(uri)
        currentImageBytes.postValue(byteArray)
    }

    private suspend fun uriToByteArray(uri: Uri): ByteArray {
        return withContext(Dispatchers.IO) {
            val inputStream = contentResolver.openInputStream(uri)
            val outputStream = ByteArrayOutputStream()
            inputStream.use { input ->
                val buffer = ByteArray(1024)
                var read: Int
                while (input?.read(buffer).also { read = it ?: -1 } != -1) {
                    outputStream.write(buffer, 0, read)
                }
            }
            return@withContext outputStream.toByteArray()
        }
    }

    fun addNewBook(book: Book){
        // Adding book to the library
        libraryDetail.books.add(book)

        // Saving book to db and updating library
        viewModelScope.launch {
            repository.addBook(book)
            repository.updateLibrary(libraryDetail)
        }

        // Updating library

    }
}