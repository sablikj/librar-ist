package pt.ulisboa.tecnico.cmov.librarist.screens.map.detail

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import dagger.hilt.android.lifecycle.HiltViewModel
import pt.ulisboa.tecnico.cmov.librarist.model.library.Library
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants
import javax.inject.Inject

@HiltViewModel
class LibraryDetailViewModel @Inject constructor(
    //private val repository: Repository,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _scanResult = MutableLiveData<String>()
    val scanResult: LiveData<String> get() = _scanResult

    val loading = mutableStateOf(false)
    private val libraryId: Int? = savedStateHandle[Constants.Routes.LIBRARY_DETAIL_ID]

    var libraryDetail by mutableStateOf(Library())
        private set

    // Bar code scanner
    val options = GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_EAN_13
        )
        .build()

    init {
        //TODO: Implement
        /*
        libraryId?.let {
            loading.value = true
            viewModelScope.launch(Dispatchers.IO) {
                repository.refreshLibraryDetail(libraryId)
                repository.getLibraryDetail(it).collect {detail ->
                    withContext(Dispatchers.Main) {
                        if (detail != null) {
                            libraryDetail = detail
                            loading.value = false
                        }
                    }
                }
            }
        } */
    }

    fun checkIn(context: Context) {
        val scanner = GmsBarcodeScanning.getClient(context, options)
        scanner.startScan()
            .addOnSuccessListener { barcode ->
                // Task completed successfully
                _scanResult.value = barcode.rawValue
                barcode.rawValue?.let { Log.d("barcode", it) }
            }
            .addOnCanceledListener {
                // Task canceled
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
            }
    }

    fun checkOut(){
        //TODO: Implement
        // Should open the camera and scan the barcode, then remove from library
    }

    fun addFavourite(){
        //TODO: Implement
    }

    fun removeFavourite(){
        //TODO: Implement
    }
}
/*
@Composable
fun CheckInScreen(viewModel: LibraryDetailViewModel = hiltViewModel()) {
    val scanResult by viewModel.scanResult.observeAsState("")
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.checkIn(context)
    }

    if (scanResult.isNotEmpty()) {
        Snackbar(){
            Text(text = scanResult)
        }
    }
}*/