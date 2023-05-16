package pt.ulisboa.tecnico.cmov.librarist.screens.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import pt.ulisboa.tecnico.cmov.librarist.screens.search.SearchViewModel

@Composable
fun MapScreen(
    state: MapState,
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(false) }
    permissionGranted = viewModel.checkLocationPermission()
    var showDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            permissionGranted = true
        }else{
            showDialog = true
        }
    }

    LaunchedEffect(key1 = Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Permission Denied") },
            text = { Text(text = "Location features won't work without the location permission.") },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = "OK")
                }
            }
        )
    }

    val mapProperties = MapProperties(
        isMyLocationEnabled = permissionGranted
    )
    val mapUiSettings = MapUiSettings(
        zoomControlsEnabled = false,
        mapToolbarEnabled = true,
        myLocationButtonEnabled = true
    )

    val cameraPositionState = rememberCameraPositionState()
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            properties = mapProperties,
            cameraPositionState = cameraPositionState,
            uiSettings = mapUiSettings

        ) {
            MarkerInfoWindow(
                state = rememberMarkerState(position = LatLng(49.1, -122.5)),
                snippet = "Some stuff",
                onClick = {
                    System.out.println("Mitchs_: Cannot be clicked")
                    true
                },
                draggable = true
            )

            LaunchedEffect(state.lastKnownLocation) {
                state.lastKnownLocation?.let { location ->
                    cameraPositionState.centerOnLocation(scope, location)
                }
            }
        }
    }
}

fun CameraPositionState.centerOnLocation(
    coroutineScope: CoroutineScope,
    location: Location
) {
    coroutineScope.launch {
        val update = CameraUpdateFactory.newLatLngZoom(
            LatLng(location.latitude, location.longitude),
            15f
        )
        animate(update)
    }
}
