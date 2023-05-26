package pt.ulisboa.tecnico.cmov.librarist.screens.map.detail

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Observer
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.ulisboa.tecnico.cmov.librarist.R
import pt.ulisboa.tecnico.cmov.librarist.model.Book
import pt.ulisboa.tecnico.cmov.librarist.screens.camera.CameraView
import pt.ulisboa.tecnico.cmov.librarist.screens.camera.getCameraProvider
import pt.ulisboa.tecnico.cmov.librarist.screens.common.CircularProgressBar
import pt.ulisboa.tecnico.cmov.librarist.screens.map.centerOnLocation

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LibraryDetailScreen(
    onBookClicked: (String) -> Unit
) {
    val viewModel = hiltViewModel<LibraryDetailViewModel>()
    val library = viewModel.libraryDetail
    val loading = viewModel.loading

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraPositionState = rememberCameraPositionState {}
    val showBookDialog = viewModel.showBookDialog

    // Adding new library trigger
    val addNewBook = remember { mutableStateOf(false) }
    val showCamera = remember { mutableStateOf(false) }
    val stopCamera = remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // New book form
    val name = remember { mutableStateOf("") }
    val author = remember { mutableStateOf("") }
    val photoUri = remember { mutableStateOf("") }

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

    if(showBookDialog.value){
        NewBookDialog(viewModel.scanResult, name, author, showCamera, showBookDialog, photoUri, addNewBook, viewModel)
    }

    // Adding new book
    val imageObserver = remember(lifecycleOwner) {
        Observer<ByteArray> { imageBytes ->
            if (addNewBook.value) {
                val newBook = viewModel.scanResult.value?.let {
                    Book(
                        barcode = it,
                        name = name.value,
                        image = imageBytes,
                        author = author.value,
                        notifications = false,
                        available = true,
                        libraries = mutableListOf()
                    )
                }
                if (newBook != null) {
                    viewModel.addNewBook(newBook)
                    Toast.makeText(context, "New book added!", Toast.LENGTH_SHORT).show()
                }

                // Reset the trigger after the library has been added
                addNewBook.value = false
            }
        }
    }
    // Updating library locally when it changes
    LaunchedEffect(viewModel.libraryDetail.books.size) {
        viewModel.repository.updateLibrary(library)
    }

    // Triggered after barcode is scanned
    if(viewModel.processBarCode.value){
        LaunchedEffect(viewModel.scanResult.value) {
            if(viewModel.scanResult.value != ""){
                val book = withContext(Dispatchers.IO) {viewModel.scanResult.value?.let { viewModel.repository.getBook(it)}}
                Log.d("book", "detail book: $book")

                if(book == null){
                    if(viewModel.checkIn.value){
                        // Check-in
                        viewModel.showBookDialog.value = true
                    }
                    else{
                        // Check-out
                        Toast.makeText(context, "Scanned book is not in this library.", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    if(viewModel.checkIn.value){
                        // Check-in
                        viewModel.libraryDetail.books.add(book)
                        Toast.makeText(context, "Book successfully added to this library.", Toast.LENGTH_SHORT).show()
                    }else{
                        // Check-out
                        // If scanned book is in this library, remove it
                        if (viewModel.libraryDetail.books.any { it.barcode == book.barcode }) {
                            Log.d("books", viewModel.libraryDetail.books.size.toString())
                            val index = viewModel.libraryDetail.books.indexOfFirst { it.barcode == book.barcode }
                            if (index >= 0) {
                                viewModel.libraryDetail.books.removeAt(index)
                            }
                            Log.d("books", viewModel.libraryDetail.books.size.toString())
                        }else{
                            Toast.makeText(context, "Scanned book is not in this library.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            else{
                Toast.makeText(context, "Book was not scanned successfully", Toast.LENGTH_SHORT).show()
            }
            viewModel.processBarCode.value = false
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
                                    .padding(4.dp)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,

                                ){
                                // Check-in button
                                Button(
                                    onClick = {
                                        // true for check-in
                                        viewModel.ProcessBook(context, true)
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
                                        // false for checkout
                                        viewModel.ProcessBook(context, false)
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
                    ElevatedCard(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp) // change to a height that suits your needs
                            .padding(vertical = 6.dp)
                    ) {
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
                        //Available books
                        if(library.books.isNotEmpty()){
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(library.books) { book ->
                                    Card(modifier = Modifier
                                        .fillMaxWidth(),
                                        onClick = { onBookClicked(book.barcode) }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .padding(6.dp)
                                                .fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = book.name,
                                                    style = MaterialTheme.typography.headlineSmall,
                                                    modifier = Modifier.padding(end = 8.dp)
                                                )
                                                Text(
                                                    text = book.author,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                )
                                            }
                                            Image(
                                                painter = rememberAsyncImagePainter(model = book.image),
                                                contentDescription = "Book cover image",
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                            )
                                        }
                                    }
                                }
                            }
                        }else{
                            Text(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 6.dp),
                                text = "No available books",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
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
                    showBookDialog.value = true
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
