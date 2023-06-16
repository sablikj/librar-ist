package pt.ulisboa.tecnico.cmov.librarist.screens.search.detail

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import pt.ulisboa.tecnico.cmov.librarist.R
import pt.ulisboa.tecnico.cmov.librarist.screens.common.CircularProgressBar

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    navController: NavHostController,
    onLibraryClicked: (String) -> Unit
) {
    val viewModel = hiltViewModel<BookDetailViewModel>()
    val book = viewModel.bookDetail
    val loading = viewModel.loading
    val notifications by viewModel.notifications.collectAsState()
    var context = LocalContext.current
    val lifecycleScope = rememberCoroutineScope()

    val imagePainter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(book.image)
            .crossfade(durationMillis = 1000)
            .error(R.drawable.ic_placeholder)
            .placeholder(R.drawable.ic_placeholder)
            .build(),
        contentScale = ContentScale.Fit
    )

// Connect to the WebSocket when the screen is shown
    LaunchedEffect(Unit) {
        viewModel.connectWebSocket()
    }

// Remember to close the connection when the screen is dismissed
    DisposableEffect(Unit) {
        onDispose {
            viewModel.disconnectWebSocket()
        }
    }
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
                        viewModel.shareBook(context, lifecycleScope)
                    }) {
                        Icon(painter = painterResource( R.drawable.baseline_share_24),
                            contentDescription = "Share book",
                        )
                    }
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
                },
                navigationIcon = { IconButton(
                    onClick = { navController.popBackStack() },
                    content = { Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24),
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(align = Alignment.Center)
                    )}
                )},

            )
        },
        content = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(top = 70.dp)
                    .padding(horizontal = 6.dp, vertical = 6.dp)
                    .fillMaxWidth()
            ) {
                CircularProgressBar(isDisplayed = loading.value)

                if (!loading.value) {
                    // Main card
                    ElevatedCard(
                        colors = CardDefaults.cardColors(
                            containerColor =  MaterialTheme.colorScheme.primary,
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Image(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(CardDefaults.elevatedShape),
                                painter = imagePainter,
                                contentDescription = book.name,
                                contentScale = ContentScale.Crop,
                                )
                            // Name
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = book.name,
                                    color = Color.Black,
                                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            // Author
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ){
                                Text(
                                    text = book.author,
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Other libraries card
                    ElevatedCard(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(vertical = 12.dp)
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
                                text = context.getString(R.string.locations),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        if (viewModel.libraries.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
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
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = context.getString(R.string.not_available),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}