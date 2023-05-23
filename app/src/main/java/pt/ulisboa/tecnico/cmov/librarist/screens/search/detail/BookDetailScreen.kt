package pt.ulisboa.tecnico.cmov.librarist.screens.search.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import pt.ulisboa.tecnico.cmov.librarist.R
import pt.ulisboa.tecnico.cmov.librarist.screens.common.CircularProgressBar

@Composable
fun BookDetailScreen()
{
    val viewModel = hiltViewModel<BookDetailViewModel>()
    val book = viewModel.bookDetail
    val loading = viewModel.loading

    val imagePainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(book.image)
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
                            contentDescription = book.name,
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
                                text = book.name.toString(),
                                color = Color.Black,
                                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                                fontWeight = FontWeight.Bold,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Available
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
                                text = "Availability",
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                fontWeight = FontWeight.Bold
                            )
                            if (book.available){
                                Text(
                                    modifier = Modifier
                                        .padding(6.dp),
                                    text = "Available",
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                    fontWeight = FontWeight.Normal
                                )
                            }else{
                                Text(
                                    modifier = Modifier
                                        .padding(6.dp),
                                    text = "Not available",
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
            // Other libraries card
            if(book.available){
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
                                    text = "Also available here",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            //TODO: Implement
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
                                    text = "Alameda",
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    modifier = Modifier
                                        .padding(6.dp),
                                    text = "700 m",
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                    fontWeight = FontWeight.Normal,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}