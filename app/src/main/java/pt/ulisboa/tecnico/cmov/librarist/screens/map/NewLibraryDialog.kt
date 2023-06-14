package pt.ulisboa.tecnico.cmov.librarist.screens.map

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.android.gms.maps.model.LatLng
import pt.ulisboa.tecnico.cmov.librarist.R

@Composable
fun NewLibraryDialog(
    name: MutableState<String>,
    address: MutableState<String>,
    location: MutableState<LatLng>,
    showCamera: MutableState<Boolean>,
    showLibraryDialog: MutableState<Boolean>,
    photo_uri: MutableState<String>,
    addNewLibrary: MutableState<Boolean>,
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
        title = { Text(context.getString(R.string.new_library)) },
        text = {
            Column {
                // Name
                TextField(
                    value = name.value,
                    onValueChange = { newName -> name.value = newName },
                    label = { Text(context.getString(R.string.library_name)) }
                )

                // If no is available address, display location instead
                if(address.value.contains("null")){
                    // Location
                    TextField(
                        value = "${location.value.latitude} | ${location.value.longitude}",
                        enabled = false,
                        onValueChange = {},
                        label = { Text(context.getString(R.string.location)) }
                    )
                }else{
                    // Address
                    TextField(
                        value = address.value,
                        onValueChange = { newAddress: String -> address.value = newAddress },
                        label = { Text(context.getString(R.string.library_address)) }
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
                    Text(context.getString(R.string.take_photo))
                }
                Button(onClick = {
                    if(photo_uri.value.isEmpty()){
                        Toast.makeText(context, context.getText(R.string.library_photo_required), Toast.LENGTH_SHORT).show()
                    }else if (name.value.isEmpty()){
                        Toast.makeText(context, context.getText(R.string.library_name_required), Toast.LENGTH_SHORT).show()
                    }
                    else{
                        viewModel.uriToImage(Uri.parse(photo_uri.value))
                        shouldDismiss.value = true
                        addNewLibrary.value = true
                    }
                }) {
                    Text(context.getString(R.string.confirm))
                }
            }
        }
    )
}