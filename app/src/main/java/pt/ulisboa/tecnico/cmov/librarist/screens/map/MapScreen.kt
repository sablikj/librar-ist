package pt.ulisboa.tecnico.cmov.librarist.screens.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import pt.ulisboa.tecnico.cmov.librarist.R
import pt.ulisboa.tecnico.cmov.librarist.model.library.Library
import pt.ulisboa.tecnico.cmov.librarist.screens.map.camera.CameraPreviewView
import pt.ulisboa.tecnico.cmov.librarist.screens.map.camera.CameraUIAction
import pt.ulisboa.tecnico.cmov.librarist.screens.map.camera.getCameraProvider
import pt.ulisboa.tecnico.cmov.librarist.screens.map.camera.takePicture

@SuppressLint("UnrememberedMutableState")
@Composable
fun MapScreen(
    navController: NavController,
    state: MapState,
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    // Map markers
    val libraries by viewModel.state.value.libraries

    // Camera related
    val snackbarHostState = remember { SnackbarHostState() }
    var showCamera = remember { mutableStateOf(false) }
    var stopCamera = remember { mutableStateOf(false) }

    // Location related
    val cameraPositionState = rememberCameraPositionState()
    val lastKnownLocation by viewModel.lastKnownLocation.observeAsState()

    // Adding new library
    var newLibrary: Library? = null
    var showLibraryDialog = remember { mutableStateOf(false)}
    val location: MutableState<LatLng> = mutableStateOf(LatLng(0.0,0.0))
    var showPin = remember { mutableStateOf(false) }

    // New library form
    var name = remember { mutableStateOf("") }
    var address = remember { mutableStateOf("") }
    var photoUri = remember { mutableStateOf<String>("") }

    // Permissions
    var permissionGranted by remember { mutableStateOf(false) }
    var camStorGranted by remember { mutableStateOf(false) }
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
        isMyLocationEnabled = permissionGranted
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
            val newLibrary = Library(
                name = name.value,
                image = imageBytes,
                location = location.value,
                books = mutableListOf() // empty list for now
            )
            viewModel.addLibrary(newLibrary)
            Toast.makeText(context, "New library added!", Toast.LENGTH_SHORT).show()
        }
    }
    LaunchedEffect(viewModel.currentImageBytes) {
        viewModel.currentImageBytes.observe(lifecycleOwner, imageObserver)
    }

    DisposableEffect(imageObserver) {
        onDispose {
            viewModel.currentImageBytes.removeObserver(imageObserver)
        }
    }

    // Getting current location
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
            libraries.forEach { library ->
                Marker(
                    state = MarkerState(position = library.location),
                    title = library.name,
                )
            }
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
                ComposeMapCenterPointMapMarker(scope, showLibraryDialog, state.lastKnownLocation, location, showPin)
            }
        }

        if (showLibraryDialog.value) {
            //TODO: FIX - address is always same
            address.value = viewModel.getReadableLocation(location.value, context)
            NewLibraryDialog(name, address, location, showCamera, showLibraryDialog, photoUri)
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
fun NewLibraryDialog(
    name: MutableState<String>,
    address: MutableState<String>,
    location: MutableState<LatLng>,
    showCamera: MutableState<Boolean>,
    showLibraryDialog: MutableState<Boolean>,
    photo_uri: MutableState<String>,
    viewModel: MapViewModel = hiltViewModel()
){
    val context = LocalContext.current
    val shouldDismiss = remember { mutableStateOf(false) }

    // Returning values
    if (shouldDismiss.value){
        shouldDismiss.value = false
        showLibraryDialog.value = false
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
                Spacer(modifier = Modifier.padding(PaddingValues(8.dp)),)
                if(photo_uri.value != ""){
                    val imagePainter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(photo_uri.value)
                            .error(R.drawable.ic_placeholder)
                            .placeholder(R.drawable.ic_placeholder)
                            .build(),
                        contentScale = ContentScale.Fit
                    )

                    Image(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CardDefaults.elevatedShape)
                            .background(MaterialTheme.colorScheme.primary),
                        painter = imagePainter,
                        contentDescription = name.value,
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        showCamera.value = true
                        shouldDismiss.value = true
                    }) {
                    Text("Take Photo")
                }
                Button(onClick = {
                    viewModel.uriToImage(Uri.parse(photo_uri.value))
                    shouldDismiss.value = true
                }) {
                    Text("Confirm")
                }
            }
        }
    )
}


@Composable
fun ComposeMapCenterPointMapMarker(
    scope:CoroutineScope, showForm: MutableState<Boolean>,
    currentLocation: LatLng, location: MutableState<LatLng>,
    showPin: MutableState<Boolean>) {
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

// Camera
@Composable
fun CameraView(onImageCaptured: (Uri, Boolean) -> Unit, onError: (ImageCaptureException) -> Unit) {

    val context = LocalContext.current
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    val imageCapture: ImageCapture = remember {
        ImageCapture.Builder().build()
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