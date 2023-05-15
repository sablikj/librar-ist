package pt.ulisboa.tecnico.cmov.librarist.screens.map

import android.location.Location
import pt.ulisboa.tecnico.cmov.librarist.clusters.ZoneClusterItem

data class MapState(
    val lastKnownLocation: Location?,
    val clusterItems: List<ZoneClusterItem>,
)
