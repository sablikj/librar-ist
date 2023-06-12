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
import pt.ulisboa.tecnico.cmov.librarist.MapApplication
import pt.ulisboa.tecnico.cmov.librarist.data.Repository
import pt.ulisboa.tecnico.cmov.librarist.model.Library
import pt.ulisboa.tecnico.cmov.librarist.utils.ImageUtils
import pt.ulisboa.tecnico.cmov.librarist.utils.LocationUtils
import java.io.ByteArrayOutputStream
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }

    @Singleton
    @Provides
    fun provideApplication(@ApplicationContext app: Context): MapApplication {
        return app as MapApplication
    }
}

@HiltViewModel
class MapViewModel @Inject constructor(application: Application,
                                       val locationUtils: LocationUtils,
                                       val imageUtils: ImageUtils,
                                       val repository: Repository,
                                       private val contentResolver: ContentResolver
): ViewModel()
{
    val state: MutableState<MapState> = mutableStateOf(
        MapState(
            lastKnownLocation = mutableStateOf(LatLng(1.35, 103.87)),
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
    val context: Context = application.applicationContext
    // TODO: use variable from state
    val lastKnownLocation: MutableLiveData<Location> = MutableLiveData()
    var searchLocation = MutableLiveData<LatLng?>(null)

    init {
        updateLibraries(application.applicationContext)
    }

    // Converting URI to byteArray
    fun uriToImage(uri: Uri) = viewModelScope.launch {
        val byteArray = imageUtils.uriToByteArray(contentResolver, uri)
        currentImageBytes.postValue(byteArray)
    }

    fun addLibrary(library: Library) {
        viewModelScope.launch {
            repository.addLibrary(library)
            updateLibraries(context)
        }
    }

    fun updateLibraries(context: Context) {
        viewModelScope.launch {
            val libs = withContext(Dispatchers.IO) {
               repository.getLibraries(context)
            }
            state.value.libraries.value = libs
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