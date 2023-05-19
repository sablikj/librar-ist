package pt.ulisboa.tecnico.cmov.librarist.screens.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
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
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import pt.ulisboa.tecnico.cmov.librarist.R
import pt.ulisboa.tecnico.cmov.librarist.screens.map.camera.CameraPreviewView
import pt.ulisboa.tecnico.cmov.librarist.screens.map.camera.CameraUIAction
import pt.ulisboa.tecnico.cmov.librarist.screens.map.camera.takePicture

@SuppressLint("UnrememberedMutableState")
@Composable
fun MapScreen(
    navController: NavController,
    state: MapState,
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val showFormDialog =  remember { mutableStateOf(false) }

    // Camera related
    val snackbarHostState = remember { SnackbarHostState() }
    var showCamera = remember { mutableStateOf(false) }

    // Location related
    val cameraPositionState = rememberCameraPositionState()
    val lastKnownLocation by viewModel.lastKnownLocation.observeAsState()

    // Adding new library
    var showLibraryDialog = remember { mutableStateOf(false)}
    val location: MutableState<LatLng> = mutableStateOf(state.lastKnownLocation)
    var showPin by remember { mutableStateOf(false) }

    // New library form
    var name = remember { mutableStateOf("") }
    var address = remember { mutableStateOf("") }
    var photo = remember { mutableStateOf<String?>(null) }

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
            MarkerInfoWindow(
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
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            onClick = { showPin = true }
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add library")
        }

        if (showPin) {
            // Check camera permission first
            if(!viewModel.checkCameraPermission()){
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
            ComposeMapCenterPointMapMarker(scope, showLibraryDialog, state.lastKnownLocation, location)
        }


        if (showLibraryDialog.value) {
            address.value = viewModel.getReadableLocation(location.value, context)
            NewLibraryDialog(name, address, location, showCamera )
        }

        if (showCamera.value) {
            CameraView(onImageCaptured = { uri, fromGallery ->
                Log.d("camera", "Image Uri Captured from Camera View")
                //showLibraryDialog.value = true
                //Todo : use the uri as needed
                showLibraryDialog.value = true
            }, onError = { imageCaptureException ->
                scope.launch {
                    snackbarHostState.showSnackbar("An error occurred while trying to take a picture")
                }
            })
        }
    }
}

@Composable
fun NewLibraryDialog(
    name: MutableState<String>,
    address: MutableState<String>,
    location: MutableState<LatLng>,
    showCamera: MutableState<Boolean>
){
    val shouldDismiss = remember { mutableStateOf(false) }

    // Returning values
    if (shouldDismiss.value){
        return
    }

    AlertDialog(
        onDismissRequest = {},
        title = { Text("New library") },
        text = {
            Column {
                // Name
                TextField(
                    value = name.value,
                    onValueChange = { newName -> name.value = newName },
                    label = { Text("Name") }
                )

                // If no is available address, display location instead
                if(address.value.contains("null")){
                    // Location
                    TextField(
                        value = "${location.value.latitude} | ${location.value.longitude}",
                        enabled = false,
                        onValueChange = {},
                        label = { Text("Location") }
                    )
                }else{
                    // Address
                    TextField(
                        value = address.value,
                        onValueChange = { newAddress: String -> address.value = newAddress },
                        label = { Text("Address") }
                    )
                }

                // TODO: implement camera
                Button(onClick = {
                    showCamera.value = true
                    shouldDismiss.value = true
                }) {
                    Text("Take Photo")
                }
                Text("Photo: ")
            }
        },
        confirmButton = {
            Button(onClick = {
                shouldDismiss.value = true }) {
                Text("Confirm")
            }
        }
    )
}


@Composable
fun ComposeMapCenterPointMapMarker(
    scope:CoroutineScope, showForm: MutableState<Boolean>,
    currentLocation: LatLng, location: MutableState<LatLng>) {
    val shouldDismiss = remember { mutableStateOf(false) }

    //TODO: should be current location
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, 18f)
        centerOnLocation(scope, currentLocation)
    }

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

// Camera
@Composable
fun CameraView(onImageCaptured: (Uri, Boolean) -> Unit, onError: (ImageCaptureException) -> Unit) {

    val context = LocalContext.current
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    val imageCapture: ImageCapture = remember {
        ImageCapture.Builder().build()
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) onImageCaptured(uri, true)
    }

    CameraPreviewView(
        imageCapture,
        lensFacing
    ) { cameraUIAction ->
        when (cameraUIAction) {
            is CameraUIAction.OnCameraClick -> {
                imageCapture.takePicture(context, lensFacing, onImageCaptured, onError)
            }
            is CameraUIAction.OnAcceptImageClick -> {
                //TODO: implement
            }
            is CameraUIAction.OnDenyImageClick -> {
                //TODO: implement
            }
        }
    }
}