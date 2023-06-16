package pt.ulisboa.tecnico.cmov.librarist.screens.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
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
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.maps.android.compose.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import pt.ulisboa.tecnico.cmov.librarist.R
import pt.ulisboa.tecnico.cmov.librarist.model.Library
import pt.ulisboa.tecnico.cmov.librarist.screens.camera.CameraView
import pt.ulisboa.tecnico.cmov.librarist.screens.camera.getCameraProvider
import pt.ulisboa.tecnico.cmov.librarist.screens.map.detail.centerOnLocation
import androidx.compose.material.*
import java.util.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import pt.ulisboa.tecnico.cmov.librarist.utils.checkCameraPermission
import pt.ulisboa.tecnico.cmov.librarist.utils.checkLocationPermission


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnrememberedMutableState")
@Composable
fun MapScreen(
    state: MutableState<MapState>,
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
    locationPermissionGranted = checkLocationPermission(context)
    var showDialog by remember { mutableStateOf(false) }

    // Searchbar
    var searchText by remember { mutableStateOf("") }
    var searchActive by remember { mutableStateOf(false) }
    val searchPredictions = remember { mutableStateListOf<AutocompletePrediction>() }
    val searchLocation by viewModel.searchLocation.observeAsState()


    // Updating libraries after return to main page (favourite libs..)
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.updateLibraries(context)
                }
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            locationPermissionGranted = true
            viewModel.locationPermissionGranted.value = true
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
                Toast.makeText(context, context.getText(R.string.library_added), Toast.LENGTH_SHORT).show()

                // Reset the trigger after the library has been added
                addNewLibrary.value = false
            }
        }
    }

    // Triggered when search location is updated
    searchLocation?.let { latLng ->
        LaunchedEffect(latLng) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 18f)

            // updating last known location (for new library map overlay)
            val newLocation = Location(LocationManager.GPS_PROVIDER)
            newLocation.latitude = latLng.latitude
            newLocation.longitude = latLng.longitude
            viewModel.lastKnownLocation.value = newLocation
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
        if (!camStorGranted) {
            var permissions = arrayOf(Manifest.permission.CAMERA)

            if(android.os.Build.VERSION.SDK_INT <=android.os.Build.VERSION_CODES.Q){
                permissions+=Manifest.permission.WRITE_EXTERNAL_STORAGE
            }
            requestMultiplePermissions.launch(permissions)
        } else {
            camStorGranted = true
        }

        // Center map on current location
        val loc =  viewModel.locationUtils.getLastKnownLocation(context)
        if (loc != null) {
            state.value.lastKnownLocation.value = LatLng(loc.latitude, loc.longitude)
        }
        cameraPositionState.position = CameraPosition.fromLatLngZoom(state.value.lastKnownLocation.value, 18f)
    }

    // Triggered when location is obtained for the first time
    LaunchedEffect(viewModel.initialLocation) {
        viewModel.initialLocation.value?.let { location ->
            val latLng = LatLng(location.latitude, location.longitude)
            cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 18f)
        }
    }

    // Permission denied dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = context.getString(R.string.permission_denied)) },
            text = { Text(text = context.getString(R.string.permission_denied_description)) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = context.getString(R.string.ok))
                }
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        SearchBar(
            modifier = Modifier.align(Alignment.TopCenter),
            query = searchText,
            onQueryChange = { query ->
                searchText = query
                if (query.isNotBlank()) {
                    viewModel.getPredictions(query) { result ->
                        searchPredictions.clear()
                        result?.forEach { prediction ->
                            searchPredictions.add(prediction)
                        }
                    }
                }
            },
            onSearch = {
                searchActive = false
            },
            active = searchActive,
            onActiveChange = {
                searchActive = it
            },
            placeholder = {
                Text(text = context.getString(R.string.search))
            },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search icon")
            },
            trailingIcon = {
                if(searchActive){
                    Icon(
                        modifier = Modifier.clickable {
                            if(searchText.isNotEmpty()){
                                searchText = ""
                            }else{
                                searchActive = false
                            }
                        },
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close icon"
                    )
                }
            }
        ) {
            LazyColumn {
                items(searchPredictions) { prediction ->
                    Text(
                        modifier = Modifier
                            .padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
                            .clickable {
                                viewModel.fetchPlaceDetails(prediction.placeId)
                                searchActive = false
                                searchText = ""
                                searchPredictions.clear()
                            },
                        text = prediction.getPrimaryText(null).toString(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            properties = mapProperties,
            cameraPositionState = cameraPositionState,
            uiSettings = mapUiSettings,
            contentPadding = PaddingValues(0.dp,64.dp,8.dp,64.dp)

        ) {
            // Adding library markers
            viewModel.state.value.libraries.value.forEach { library ->
                val markerState = rememberMarkerState(position = library.location)
                Marker(
                    icon = if(library.favourite) BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN) else BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                    state = markerState,
                    title = library.name,
                    onInfoWindowClick = {
                        // Open only if Camera and storage permissions are granted
                        scope.launch {
                            if(camStorGranted) {
                                onMarkerClicked(library.id)
                            }
                        }
                    }
                )
                // Automatically open the info window if the user's location is within 100 meters
                lastKnownLocation?.let { userLocation ->
                    val distance = viewModel.locationUtils.getDistance(LatLng(userLocation.latitude, userLocation.longitude), library.location)
                    if (distance <= 100) {
                        markerState.showInfoWindow()
                    }
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
            if(!checkCameraPermission(LocalContext.current)){
                var permissions = arrayOf(Manifest.permission.CAMERA)
                if(android.os.Build.VERSION.SDK_INT <=android.os.Build.VERSION_CODES.Q){
                    permissions+=Manifest.permission.WRITE_EXTERNAL_STORAGE
                }
                requestMultiplePermissions.launch(permissions)
            }else{
                camStorGranted = true
            }
            // Open only if Camera and storage permissions are granted
            if(camStorGranted){
                ComposeMapCenterPointMapMarker(scope, showLibraryDialog,
                    state.value.lastKnownLocation.value, viewModel.location, showPin)
            }
        }

        if (showLibraryDialog.value) {
            address.value = viewModel.locationUtils.getReadableLocation(viewModel.location.value, context)
            NewLibraryDialog(name, address, viewModel.location, showCamera, showLibraryDialog, photoUri, addNewLibrary)
        }

        if (showCamera.value) {
            CameraView(onImageCaptured = { uri, _ ->
                photoUri.value = uri.toString()
                stopCamera.value = true
            }, onError = {
                scope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.camera_error))
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

