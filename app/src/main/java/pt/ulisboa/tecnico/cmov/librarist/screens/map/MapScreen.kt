package pt.ulisboa.tecnico.cmov.librarist.screens.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import pt.ulisboa.tecnico.cmov.librarist.R

@SuppressLint("UnrememberedMutableState")
@Composable
fun MapScreen(
    state: MapState,
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()
    val scope = rememberCoroutineScope()
    val lastKnownLocation by viewModel.lastKnownLocation.observeAsState()


    var showForm by remember { mutableStateOf(false) }
    var showPin by remember { mutableStateOf(false) }

    // New library form
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var photo by remember { mutableStateOf<String?>(null) }

    // Permissions
    var permissionGranted by remember { mutableStateOf(false) }
    permissionGranted = viewModel.checkLocationPermission()
    var showDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            permissionGranted = true
        } else {
            showDialog = true
        }
    }

    val mapProperties = MapProperties(
        isMyLocationEnabled = permissionGranted
    )
    val mapUiSettings = MapUiSettings(
        zoomControlsEnabled = true,
        mapToolbarEnabled = true,
        myLocationButtonEnabled = true,
        compassEnabled = true
    )

    LaunchedEffect(key1 = Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }else{
            viewModel.getLastKnownLocation(context)

            // Center map on current location
            cameraPositionState.position = CameraPosition.fromLatLngZoom(state.lastKnownLocation, 18f)
        }
    }

    LaunchedEffect(lastKnownLocation) {
        lastKnownLocation?.let { location ->
            val latLng = LatLng(location.latitude, location.longitude)
            cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 18f)
        }
    }

    // Permission denied dialog
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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            properties = mapProperties,
            cameraPositionState = cameraPositionState,
            uiSettings = mapUiSettings,
            contentPadding = PaddingValues(0.dp,0.dp,8.dp,64.dp)

        ) {
            MarkerInfoWindow(
                snippet = "Some stuff",
                onClick = {
                    System.out.println("Mitchs_: Cannot be clicked")
                    true
                },
                draggable = true
            )

            // Not doing anything??

            LaunchedEffect(state.lastKnownLocation) {
                state.lastKnownLocation?.let { location ->
                    cameraPositionState.centerOnLocation(scope, location)
                }
            }
        }
        FloatingActionButton(
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            onClick = { showPin = true }
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add library")
        }

        val show: MutableState<Boolean> = mutableStateOf(false)
        val location: MutableState<LatLng> = mutableStateOf(state.lastKnownLocation)

        if (showPin) {
            ComposeMapCenterPointMapMarker(scope, show,state.lastKnownLocation, location)
        }

        if (show.value == true) {
            address = viewModel.getReadableLocation(location.value, context)
            AlertDialog(
                onDismissRequest = {},
                title = { Text("New library") },
                text = {
                    Column {
                        // Name
                        TextField(
                            value = name,
                            onValueChange = { newName -> name = newName },
                            label = { Text("Name") }
                        )

                        // Address
                        TextField(
                            value = address,
                            onValueChange = { newAddress: String -> name = newAddress },
                            label = { Text("Address") }
                        )
                        // TODO: implement camera
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
                    Button(onClick = { show.value = false }) {
                        Text("Confirm")
                    }
                }
            )
        }
    }
}


@Composable
fun ComposeMapCenterPointMapMarker(scope:CoroutineScope, showForm : MutableState<Boolean>,currentLocation: LatLng, location: MutableState<LatLng>) {
    val shouldDismiss = remember {
        mutableStateOf(false)
    }

    //TODO: should be current location
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, 18f)
        centerOnLocation(scope, currentLocation)
    }
    //cameraPositionState.centerOnLocation(scope, currentLocation)

    // Returning values
    if (shouldDismiss.value){
        showForm.value = true
        location.value = cameraPositionState.position.target
        return
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    )
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(
            onClick = { shouldDismiss.value = true },
        ) {
            Image(
                painter = painterResource(id = R.drawable.pin),
                contentDescription = "marker",
            )
        }
    }
}

fun CameraPositionState.centerOnLocation(
    coroutineScope: CoroutineScope,
    location: LatLng
) {
    coroutineScope.launch {
        val update = CameraUpdateFactory.newLatLngZoom(
            location,15f)
        animate(update)
    }
}