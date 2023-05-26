package pt.ulisboa.tecnico.cmov.librarist.screens.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import pt.ulisboa.tecnico.cmov.librarist.R
import pt.ulisboa.tecnico.cmov.librarist.model.Library
import pt.ulisboa.tecnico.cmov.librarist.screens.camera.CameraView
import pt.ulisboa.tecnico.cmov.librarist.screens.camera.getCameraProvider

@SuppressLint("UnrememberedMutableState")
@Composable
fun MapScreen(
    state: MapState,
    onMarkerClicked: (String) -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    // Adding new library trigger
    val addNewLibrary = remember { mutableStateOf(false) }

    // Camera related
    val snackbarHostState = remember { SnackbarHostState() }
    val showCamera = remember { mutableStateOf(false) }
    val stopCamera = remember { mutableStateOf(false) }

    // Location related
    val cameraPositionState = rememberCameraPositionState()
    val lastKnownLocation by viewModel.lastKnownLocation.observeAsState()

    // Adding new library
    val showLibraryDialog = remember { mutableStateOf(false)}
    val showPin = remember { mutableStateOf(false) }

    // New library form
    val name = remember { mutableStateOf("") }
    val address = remember { mutableStateOf("") }
    val photoUri = remember { mutableStateOf<String>("") }

    // Permissions
    var locationPermissionGranted by remember { mutableStateOf(false) }
    var camStorGranted by remember { mutableStateOf(false) }
    locationPermissionGranted = viewModel.checkLocationPermission()
    var showDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            locationPermissionGranted = true
        } else {
            showDialog = true
        }
    }

    val requestMultiplePermissions =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                if (it.value) {
                    camStorGranted = true
                } else {
                    showDialog = true
                    camStorGranted = false
                }
            }
        }

    val mapProperties = MapProperties(
        isMyLocationEnabled = locationPermissionGranted
    )
    val mapUiSettings = MapUiSettings(
        zoomControlsEnabled = true,
        mapToolbarEnabled = true,
        myLocationButtonEnabled = true,
        compassEnabled = true
    )

    // Adding new Library
    val imageObserver = remember(lifecycleOwner) {
        Observer<ByteArray> { imageBytes ->
            if (addNewLibrary.value) {
                val newLibrary = Library(
                    name = name.value,
                    image = imageBytes,
                    location = viewModel.location.value,
                    books = mutableListOf()
                )
                viewModel.addLibrary(newLibrary)
                Toast.makeText(context, "New library added!", Toast.LENGTH_SHORT).show()

                // Reset the trigger after the library has been added
                addNewLibrary.value = false
            }
        }
    }
    // Triggered when library photo is converted to byte array
    LaunchedEffect(viewModel.currentImageBytes) {
        viewModel.currentImageBytes.observe(lifecycleOwner, imageObserver)
    }

    // Triggered when imageObserver finishes
    DisposableEffect(imageObserver) {
        onDispose {
            viewModel.currentImageBytes.removeObserver(imageObserver)
        }
    }

    // Getting current location
    // Triggered when location permission is granted or on permission check
    LaunchedEffect(locationPermissionGranted) {
        // Location permission
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        // Center map on current location
        viewModel.getLastKnownLocation(context)
        cameraPositionState.position = CameraPosition.fromLatLngZoom(state.lastKnownLocation, 18f)
    }

    // Triggered when lastKnownLocation is updated
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
            text = { Text(text = "Some features won't work without the permissions.") },
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
            // Adding library markers
            viewModel.state.value.libraries.value.forEach { library ->
                if(library.favourite){
                    Marker(
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                        state = MarkerState(position = library.location),
                        title = library.name,
                        onInfoWindowClick = {
                            scope.launch {
                                onMarkerClicked(library.id)
                            }
                        }
                    )
                }else{
                    Marker(
                        state = MarkerState(position = library.location),
                        title = library.name,
                        onInfoWindowClick = {
                            scope.launch {
                                onMarkerClicked(library.id)
                            }
                        }
                    )
                }
            }
            
            LaunchedEffect(state.lastKnownLocation) {
                state.lastKnownLocation.let { location ->
                    cameraPositionState.centerOnLocation(scope, location)
                }
            }
        }
        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            onClick = {
                // Reset image path
                photoUri.value = ""
                showPin.value = true
            }
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add library")
        }

        if (showPin.value) {
            // Check camera permission first
            if(!viewModel.checkCameraPermission()){
                val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestMultiplePermissions.launch(permissions)
            }else{
                camStorGranted = true
            }
            // Open only if Camera and storage permissions are granted
            if(camStorGranted){
                ComposeMapCenterPointMapMarker(scope, showLibraryDialog, state.lastKnownLocation, viewModel.location, showPin)
            }
        }

        if (showLibraryDialog.value) {
            address.value = viewModel.getReadableLocation(viewModel.location.value, context)
            NewLibraryDialog(name, address, viewModel.location, showCamera, showLibraryDialog, photoUri, addNewLibrary)
        }

        if (showCamera.value) {
            CameraView(onImageCaptured = { uri, _ ->
                photoUri.value = uri.toString()
                stopCamera.value = true
                //TODO: after saving marker, set URI to "" so it does not appear when adding another library
            }, onError = { _ ->
                scope.launch {
                    snackbarHostState.showSnackbar("An error occurred while trying to take a picture")
                }
            })

            if(stopCamera.value){
                LaunchedEffect(stopCamera.value){
                    val cameraProvider = context.getCameraProvider()
                    cameraProvider.unbindAll()
                }
                showCamera.value = false
                stopCamera.value = false
                showLibraryDialog.value = true
            }
        }
    }
}

@Composable
fun ComposeMapCenterPointMapMarker(
    scope:CoroutineScope,
    showForm: MutableState<Boolean>,
    currentLocation: LatLng,
    location: MutableState<LatLng>,
    showPin: MutableState<Boolean>
){
    val shouldDismiss = remember { mutableStateOf(false) }

    //TODO: should be current location
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, 18f)
        centerOnLocation(scope, currentLocation)
    }

    // Returning values
    if (shouldDismiss.value){
        showPin.value = false
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