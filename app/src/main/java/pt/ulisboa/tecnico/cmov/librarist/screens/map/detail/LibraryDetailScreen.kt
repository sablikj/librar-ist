package pt.ulisboa.tecnico.cmov.librarist.screens.map.detail

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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
                Box(
                    Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column {
                        Image(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CardDefaults.elevatedShape)
                                .background(MaterialTheme.colorScheme.primary),
                            painter = imagePainter,
                            contentDescription = library.name,
                            contentScale = ContentScale.Crop,
                        )
                        // Name
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = library.name.toString(),
                                color = Color.Black,
                                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                                fontWeight = FontWeight.Bold,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        // Actions
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,

                            ){
                            // Favorite button
                            // TODO: Add check if library is in favorites and add remove button
                            Button(
                                onClick = {
                                          viewModel.addFavourite()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow),
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
                                    Icons.Filled.Favorite,
                                    contentDescription = "Favorite",
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                Text("Add Favourite")
                            }
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