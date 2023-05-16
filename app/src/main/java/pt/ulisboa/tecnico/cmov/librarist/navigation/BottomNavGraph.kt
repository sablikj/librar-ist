package pt.ulisboa.tecnico.cmov.librarist.navigation


import android.content.Context
import android.location.Location
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.paging.ExperimentalPagingApi
import pt.ulisboa.tecnico.cmov.librarist.screens.map.MapState
import pt.ulisboa.tecnico.cmov.librarist.clusters.ZoneClusterItem
import pt.ulisboa.tecnico.cmov.librarist.screens.map.MapScreen
import pt.ulisboa.tecnico.cmov.librarist.screens.search.SearchScreen
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants

@OptIn(ExperimentalPagingApi::class)
@Composable
fun BottomNavGraph(navController: NavHostController) {
    val lastKnownLocation: Location? = null // Replace with your actual last known location

    // Construct your MapState
    val mapState = MapState(lastKnownLocation)

    NavHost(
        navController = navController,
        route = Constants.Graph.ROOT,
        startDestination = BottomBarScreen.Map.route
    ){
        // Main tab
        composable(route = BottomBarScreen.Map.route){
            MapScreen(
                state = mapState
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