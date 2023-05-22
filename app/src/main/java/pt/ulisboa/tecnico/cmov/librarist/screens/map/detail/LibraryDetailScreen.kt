package pt.ulisboa.tecnico.cmov.librarist.screens.map.detail

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import pt.ulisboa.tecnico.cmov.librarist.R
import pt.ulisboa.tecnico.cmov.librarist.screens.common.CircularProgressBar

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LibraryDetailScreen() {
    val viewModel = hiltViewModel<LibraryDetailViewModel>()
    val library = viewModel.libraryDetail
    val loading = viewModel.loading
    val context = LocalContext.current

    val imagePainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(library.image)
            .crossfade(durationMillis = 1000)
            .error(R.drawable.ic_placeholder)
            .placeholder(R.drawable.ic_placeholder)
            .build(),
        contentScale = ContentScale.Fit
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        library.name,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(MaterialTheme.colorScheme.primary),
                actions = {
                    IconButton(onClick = { viewModel.favourite() }) {
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
                    .padding(horizontal = 12.dp, vertical = 6.dp)
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
                                    // Inner content including an icon and a text label
                                    Icon(
                                        Icons.Filled.Add,
                                        contentDescription = "Take book",
                                        modifier = Modifier.size(ButtonDefaults.IconSize)
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
                            .padding(vertical = 6.dp)
                    ) {
                        Box(
                            Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.BottomCenter
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
                                        text = "Location",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    // Navigation button
                                    Button(
                                        onClick = { /* ... */ },
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
                                            painter = painterResource(R.drawable.baseline_assistant_direction_24),
                                            contentDescription = "Favorite",
                                            modifier = Modifier.size(ButtonDefaults.IconSize)
                                        )
                                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                        Text("Get Directions")
                                    }
                                }
                                // Map
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
                                        text = "Map goes here",
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
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
                        Box(
                            Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.BottomCenter
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
                                        text = "Map goes here",
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
        }
    )
}
