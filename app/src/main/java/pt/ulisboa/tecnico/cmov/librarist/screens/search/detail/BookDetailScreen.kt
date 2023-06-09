package pt.ulisboa.tecnico.cmov.librarist.screens.search.detail

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import pt.ulisboa.tecnico.cmov.librarist.R
import pt.ulisboa.tecnico.cmov.librarist.screens.common.CircularProgressBar

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    onLibraryClicked: (String) -> Unit
) {
    val viewModel = hiltViewModel<BookDetailViewModel>()
    val book = viewModel.bookDetail
    val loading = viewModel.loading
    val notifications by viewModel.notifications.collectAsState()

    val imagePainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(book.image)
            .crossfade(durationMillis = 1000)
            .error(R.drawable.ic_placeholder)
            .placeholder(R.drawable.ic_placeholder)
            .build(),
        contentScale = ContentScale.Fit
    )
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(modifier = Modifier.padding(bottom = 8.dp),
                title = {
                    Text(
                        book.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(MaterialTheme.colorScheme.primary),
                actions = {
                    IconButton(onClick = {
                        viewModel.onNotificationsChanged(!notifications)
                        viewModel.notifications()
                    }) {
                        if (notifications) {
                            Icon(painter = painterResource( R.drawable.baseline_notifications_24),
                                contentDescription = "Notifications",
                            )
                        }else{
                            Icon(painter = painterResource( R.drawable.baseline_notifications_off_24),
                                contentDescription = "Notifications Off",
                            )
                        }
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

                if (!loading.value) {
                    // Main card
                    ElevatedCard(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
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
                            }
                        }
                    }
                    // Other libraries card
                    //Available books
                    if (viewModel.libraries.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(viewModel.libraries) { lib ->
                                Card(modifier = Modifier
                                    .fillMaxWidth(),
                                    onClick = { onLibraryClicked(lib.id) }
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
                                                text = lib.name,
                                                style = MaterialTheme.typography.headlineSmall,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                        }
                                        Text(
                                            text = "${lib.distance} m",
                                            style = MaterialTheme.typography.headlineSmall,
                                            modifier = Modifier.padding(end = 8.dp)
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
                            text = "Not available",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    // TODO: fix
                    /*
                    if (viewModel.libraries.isNotEmpty()) {
                        ElevatedCard(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primary)
                            )
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
                                        text = "Available in",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                //TODO: Implement LazyColumn
                                Card(modifier = Modifier
                                    .fillMaxWidth(),
                                    onClick = {  }
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
                                                text = "Library",
                                                style = MaterialTheme.typography.headlineSmall,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            Text(
                                                text = "Distance",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                        }
                                        /*
                                        Image(
                                            painter = rememberAsyncImagePainter(model = book.image),
                                            contentDescription = "Book cover image",
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                        )*/
                                    }
                                }
                            }
                        }
                    }*/
                }
            }
        }
    )
}