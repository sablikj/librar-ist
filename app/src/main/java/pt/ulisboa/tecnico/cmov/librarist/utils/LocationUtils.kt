package pt.ulisboa.tecnico.cmov.librarist.utils

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.roundToInt

class LocationUtils @Inject constructor(application: Application){

    private val context: Context = application.applicationContext
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    val locationSharedFlow = MutableSharedFlow<Location>()
    val scope = CoroutineScope(Dispatchers.Main)

    suspend fun getLastKnownLocation(context: Context): Location? {
        return if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            suspendCoroutine { cont ->
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        cont.resume(location)
                    }
                    .addOnFailureListener { e ->
                        Log.d("Error", e.toString())
                        cont.resume(null)
                    }
            }
        } else {
            null
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

    fun getDistance(startPoint: LatLng, endPoint: LatLng): Int {
        val results = FloatArray(1)
        Location.distanceBetween(
            startPoint.latitude,
            startPoint.longitude,
            endPoint.latitude,
            endPoint.longitude,
            results
        )
        return results[0].roundToInt()
    }

    private val locationRequest = LocationRequest.create().apply {
        interval = 10000 // Update location every 10 seconds
        fastestInterval = 5000 // But not more frequently than every 5 seconds
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.let {
                // Here, you may want to communicate the new location back to your ViewModel or MapScreen
                // For instance, you might use a SharedFlow or LiveData to emit the new location
                scope.launch { it.lastLocation?.let { it1 -> locationSharedFlow.emit(it1) } }
            }
        }
    }

    fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}