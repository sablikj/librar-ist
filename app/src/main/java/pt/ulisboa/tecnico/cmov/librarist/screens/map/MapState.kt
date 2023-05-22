package pt.ulisboa.tecnico.cmov.librarist.screens.map

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.google.android.gms.maps.model.LatLng
import pt.ulisboa.tecnico.cmov.librarist.model.library.Library

data class MapState(
    val lastKnownLocation: LatLng,
    var libraries: MutableState<List<Library>> = mutableStateOf(listOf())
)
