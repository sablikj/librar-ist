package pt.ulisboa.tecnico.cmov.librarist.screens.map

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    state: MapState,
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    var showForm by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf<LatLng?>(null) }
    var photo by remember { mutableStateOf<String?>(null) }

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
        FloatingActionButton(
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            onClick = { showForm = true }
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add")
        }

        if (showForm) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Add New Item") },
                text = {
                    Column {
                        TextField(
                            value = name,
                            onValueChange = { newName -> name = newName },
                            label = { Text("Name") }
                        )
                        Button(onClick = {
                            // This is where you would open the map to select a location.
                            // For simplicity, we'll just use a dummy location.
                            val newLocation = LatLng(0.0, 0.0)
                            location = newLocation
                            //onLocationSelected(newLocation)
                        }) {
                            Text("Select Location")
                        }
                        Text("Location: ${location?.latitude}, ${location?.longitude}")
                        Button(onClick = {
                            // This is where you would open the camera to take a photo.
                            // For simplicity, we'll just use a dummy photo name.
                            val newPhoto = "photo.jpg"
                            photo = newPhoto
                            //onPhotoTaken(newPhoto)
                        }) {
                            Text("Take Photo")
                        }
                        Text("Photo: $photo")
                    }
                },
                confirmButton = {
                    Button(onClick = { showForm = false }) {
                        Text("Confirm")
                    }
                }
            )
        }
    }
}

@Composable
fun MyFab(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick) {
        Icon(Icons.Filled.Add, contentDescription = "Add Library")
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
