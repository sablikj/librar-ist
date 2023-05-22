package pt.ulisboa.tecnico.cmov.librarist.screens.map.detail

import android.annotation.SuppressLint
import android.util.Log
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import pt.ulisboa.tecnico.cmov.librarist.R
import pt.ulisboa.tecnico.cmov.librarist.screens.common.CircularProgressBar
import pt.ulisboa.tecnico.cmov.librarist.screens.map.centerOnLocation

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LibraryDetailScreen() {
    val viewModel = hiltViewModel<LibraryDetailViewModel>()
    val library = viewModel.libraryDetail
    val loading = viewModel.loading
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {}

    LaunchedEffect(viewModel.libraryDetail.location){
        cameraPositionState.position =  CameraPosition.fromLatLngZoom(library.location, 18f)
        cameraPositionState.centerOnLocation(scope, library.location)
    }

    val imagePainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(library.image)
            .crossfade(durationMillis = 1000)
            .error(R.drawable.ic_placeholder)
            .placeholder(R.drawable.ic_placeholder)
            .build(),
        contentScale = ContentScale.Fit
    )

    val mapProperties = MapProperties(
        isMyLocationEnabled = false
    )
    val mapUiSettings = MapUiSettings(
        zoomControlsEnabled = false,
        mapToolbarEnabled = true, // for navigation button
        myLocationButtonEnabled = false,
        compassEnabled = false
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(modifier = Modifier.padding(bottom = 8.dp),
                title = {
                    Text(
                        library.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(MaterialTheme.colorScheme.primary),
                actions = {
                    IconButton(onClick = {
                        viewModel.favourite()
                        Log.d("favourite", "Favourite clicked")
                    }) {
                        Icon(Icons.Filled.Favorite,
                            contentDescription = "Favourite",
                            tint = if (library.favourite) Color.Red else Color.Gray
                        )
                    }
                }
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 6.dp, vertical = 6.dp)
                    .fillMaxWidth()
            ) {
                CircularProgressBar(isDisplayed = loading.value)

                if(!loading.value){
                    // Main card
                    ElevatedCard(
                        colors = CardDefaults.cardColors(
                            containerColor =  MaterialTheme.colorScheme.secondaryContainer,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Column {
                            Image(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .fillMaxWidth()
                                    .clip(CardDefaults.elevatedShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                painter = imagePainter,
                                contentDescription = library.name,
                                contentScale = ContentScale.Crop,
                            )
                            // Actions
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,

                                ){
                                // Check-in button
                                Button(
                                    onClick = {
                                        viewModel.checkIn(context)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                                    // Uses ButtonDefaults.ContentPadding by default
                                    contentPadding = PaddingValues(
                                        start = 20.dp,
                                        top = 12.dp,
                                        end = 20.dp,
                                        bottom = 12.dp
                                    )
                                ) {
                                    // Inner content including an icon and a text label
                                    Icon(
                                        Icons.Filled.Add,
                                        contentDescription = "Add book",
                                        modifier = Modifier.size(ButtonDefaults.IconSize)
                                    )
                                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                    Text("Check-in")
                                }
                                // Check-out button
                                Button(
                                    onClick = {
                                        viewModel.checkOut()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                    // Uses ButtonDefaults.ContentPadding by default
                                    contentPadding = PaddingValues(
                                        start = 20.dp,
                                        top = 12.dp,
                                        end = 20.dp,
                                        bottom = 12.dp
                                    )
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Take book",
                                        modifier = Modifier.size(ButtonDefaults.IconSize),
                                        tint = Color.Red
                                    )
                                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                    Text("Check-out")
                                }
                            }
                        }
                    }
                    // Location (map) card
                    ElevatedCard(
                        colors = CardDefaults.cardColors(
                            containerColor =  MaterialTheme.colorScheme.secondaryContainer,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .padding(vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary))
                        {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primary),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 6.dp),
                                    text = "Location",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            // Map
                            GoogleMap(
                                modifier = Modifier.fillMaxSize(),
                                properties = mapProperties,
                                cameraPositionState = cameraPositionState,
                                uiSettings = mapUiSettings
                            ) {
                                Marker(
                                    state = MarkerState(position = library.location),
                                    title = library.name
                                )
                            }
                        }
                    }
                    // Available books
                    ElevatedCard(
                        colors = CardDefaults.cardColors(
                            containerColor =  MaterialTheme.colorScheme.secondaryContainer,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary))
                        {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primary),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 6.dp),
                                    text = "Available books",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            //TODO: implement available books
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ){
                                Text(
                                    modifier = Modifier
                                        .padding(6.dp),
                                    text = "Books goes here",
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    )
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
