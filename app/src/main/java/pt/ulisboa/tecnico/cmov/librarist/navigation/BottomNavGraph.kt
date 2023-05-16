package pt.ulisboa.tecnico.cmov.librarist.navigation


import android.content.Context
import android.location.Location
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.paging.ExperimentalPagingApi
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import pt.ulisboa.tecnico.cmov.librarist.screens.map.MapState
import pt.ulisboa.tecnico.cmov.librarist.clusters.ZoneClusterItem
import pt.ulisboa.tecnico.cmov.librarist.clusters.ZoneClusterManager
import pt.ulisboa.tecnico.cmov.librarist.screens.map.MapScreen
import pt.ulisboa.tecnico.cmov.librarist.screens.search.SearchScreen
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants

@OptIn(ExperimentalPagingApi::class)
@Composable
fun BottomNavGraph(navController: NavHostController) {
    // Define your clusterItems and lastKnownLocation
    val clusterItems = listOf<ZoneClusterItem>() // Replace with your actual cluster items
    val lastKnownLocation: Location? = null // Replace with your actual last known location

    // Construct your MapState
    val mapState = MapState(lastKnownLocation, clusterItems)

    val setupClusterManager: (Context, GoogleMap) -> ZoneClusterManager = { context, map ->
        ZoneClusterManager(context, map)
    }

    val calculateZoneViewCenter: () -> LatLngBounds = {
        // Initialize the builder of LatLngBounds
        val builder = LatLngBounds.builder()

        // Loop through all clusterItems and add the LatLng of each item to the builder
        for (item in clusterItems) {
            // Get the points from the polygonOptions of each ZoneClusterItem
            val points = item.polygonOptions.points
            // Add all the points to the builder
            for (point in points) {
                builder.include(point)
            }
        }

        // Build and return the LatLngBounds
        builder.build()
    }

    NavHost(
        navController = navController,
        route = Constants.Graph.ROOT,
        startDestination = BottomBarScreen.Map.route
    ){
        // Main tab
        composable(route = BottomBarScreen.Map.route){
            MapScreen(
                state = mapState,
                setupClusterManager = setupClusterManager,
                calculateZoneViewCenter = calculateZoneViewCenter
            )
        }
        // Book search tab
        composable(route = BottomBarScreen.BookSearch.route){ backStackEntry ->
            SearchScreen(
                onDetailClicked = { bookId ->
                    // To avoid duplicate navigation events
                    navController.navigate("${Constants.Routes.BOOK_DETAIL_ROUTE}/$bookId")
                    /*if (backStackEntry.lifecycle.currentState == Lifecycle.State.RESUMED) {

                    }*/
                }
            )
        }
    }
}