package pt.ulisboa.tecnico.cmov.librarist.screens.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Geocoder
import android.location.Location
import android.media.ExifInterface
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.ulisboa.tecnico.cmov.librarist.data.Repository
import pt.ulisboa.tecnico.cmov.librarist.model.Library
import java.io.ByteArrayOutputStream
import java.util.Locale
import javax.inject.Inject


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }
}

@HiltViewModel
class MapViewModel @Inject constructor(application: Application,
                                       val repository: Repository,
                                       private val contentResolver: ContentResolver
): ViewModel()
{
    val state: MutableState<MapState> = mutableStateOf(
        MapState(
            lastKnownLocation = LatLng(1.35, 103.87),
            libraries = mutableStateOf(listOf())
        )
    )

    // Places API
    private val placesClient = Places.createClient(application.applicationContext)

    // Markers
    val location = mutableStateOf(LatLng(0.0, 0.0))
    val currentImageBytes: MutableLiveData<ByteArray> = MutableLiveData()
    // Location
    @SuppressLint("StaticFieldLeak")
    private val context: Context = application.applicationContext
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    // TODO: use variable from state
    val lastKnownLocation: MutableLiveData<Location> = MutableLiveData()
    var searchLocation = MutableLiveData<LatLng?>(null)

    init {
        updateLibraries()
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

    fun addLibrary(library: Library) {
        viewModelScope.launch {
            repository.addLibrary(library)
            updateLibraries()
        }
    }

    private fun updateLibraries() {
        viewModelScope.launch {
            val libs = withContext(Dispatchers.IO) {
               repository.getLibraries()
            }
            state.value.libraries.value = libs
            Log.d("Libraries", libs.toString())
        }
    }

    @Composable
    fun checkLocationPermission(): Boolean {
        val context = LocalContext.current
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @Composable
    fun checkCameraPermission(): Boolean {
        val context = LocalContext.current
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    ////////////////////////////////
    // Location functions

    fun getLastKnownLocation(context: Context) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    lastKnownLocation.value = location
                }
                .addOnFailureListener { e ->
                    Log.d("Error", e.toString())
                }
        }
    }

    fun getReadableLocation(location: LatLng, context: Context): String {
        var addressText = ""
        val geocoder = Geocoder(context, Locale.getDefault())

        try {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

            if (addresses?.isNotEmpty() == true) {
                val address = addresses[0]
                addressText = "${address.getAddressLine(0)}, ${address.locality}"
            }
        } catch (e: Exception) {
            Log.d("geolocation", e.message.toString())
        }
        return addressText
    }

    ////////////////////////////////
    // Searchbar functions
    fun getAutocompletePredictions(
        query: String
    ): Task<FindAutocompletePredictionsResponse> {
        val requestBuilder = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
        return placesClient.findAutocompletePredictions(requestBuilder.build())
    }

    fun getPredictions(query: String, callback: (List<AutocompletePrediction>) -> Unit) {
        getAutocompletePredictions(query)
            .addOnSuccessListener { response ->
                val predictions = response.autocompletePredictions
                callback(predictions)
            }
            .addOnFailureListener { exception ->
                if (exception is ApiException) {
                    Log.e("searchbar", "Place not found: " + exception.statusCode)
                }
            }
    }

    fun fetchPlaceDetails(placeId: String) {
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)

        val fetchPlaceRequest = FetchPlaceRequest.newInstance(placeId, placeFields)
        placesClient.fetchPlace(fetchPlaceRequest)
            .addOnSuccessListener { response ->
                searchLocation.value = response.place.latLng
            }.addOnFailureListener { exception ->
                if (exception is ApiException) {
                    Log.e("searchbar", "Place not found: " + exception.statusCode)
                }
            }
    }
}