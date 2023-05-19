package pt.ulisboa.tecnico.cmov.librarist.screens.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import pt.ulisboa.tecnico.cmov.librarist.R
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class MapViewModel @Inject constructor(application: Application,): ViewModel()
{

    // Location
    @SuppressLint("StaticFieldLeak")
    private val context: Context = application.applicationContext
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    val lastKnownLocation: MutableLiveData<Location> = MutableLiveData()


    // Location related

    val state: MutableState<MapState> = mutableStateOf(
        MapState(
            lastKnownLocation = LatLng(1.35, 103.87)
        )
    )

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
                // Use the addressText in your app
                Log.d("geolocation", addressText)
            }
        } catch (e: Exception) {
            Log.d("geolocation", e.message.toString())
        }
        return addressText
    }
}