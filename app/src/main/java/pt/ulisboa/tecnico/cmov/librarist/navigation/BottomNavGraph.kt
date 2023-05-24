package pt.ulisboa.tecnico.cmov.librarist.navigation


import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.paging.ExperimentalPagingApi
import com.google.android.gms.maps.model.LatLng
import pt.ulisboa.tecnico.cmov.librarist.screens.map.MapState
import pt.ulisboa.tecnico.cmov.librarist.screens.map.MapScreen
import pt.ulisboa.tecnico.cmov.librarist.screens.map.detail.LibraryDetailScreen
import pt.ulisboa.tecnico.cmov.librarist.screens.search.SearchScreen
import pt.ulisboa.tecnico.cmov.librarist.screens.search.detail.BookDetailScreen
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants


@OptIn(ExperimentalPagingApi::class)
@Composable
fun BottomNavGraph(navController: NavHostController) {
    //TODO: Load saved lastLocation | Replace with your actual last known location
    val lastKnownLocation: LatLng = LatLng( 38.736946,  -9.142685)

    // Construct your MapState
    val mapState = MapState(lastKnownLocation)

    NavHost(
        navController = navController,
        route = Constants.Graph.ROOT,
        startDestination = BottomBarScreen.Map.route
    ){
        // Map tab
        composable(route = BottomBarScreen.Map.route){ backStackEntry ->
            MapScreen(
                state = mapState,
                onMarkerClicked = { libraryId ->
                    if(backStackEntry.getLifecycle().currentState == Lifecycle.State.RESUMED){
                        navController.navigate("${Constants.Routes.LIBRARY_DETAIL_ROUTE}/$libraryId")
                    }
                }
            )
        }
        // Library detail
        composable(
            route = "${Constants.Routes.LIBRARY_DETAIL_ROUTE}/{${Constants.Routes.LIBRARY_DETAIL_ID}}",
            arguments = listOf(
                navArgument(Constants.Routes.LIBRARY_DETAIL_ID) {
                    type = NavType.IntType
                }
            ),
        ) { backStackEntry ->
            LibraryDetailScreen(
                onBookClicked = { barcode ->
                    if(backStackEntry.getLifecycle().currentState == Lifecycle.State.RESUMED){
                        navController.navigate("${Constants.Routes.BOOK_DETAIL_ROUTE}/$barcode")
                    }
                }
            )
        }
        // Book search tab
        composable(route = BottomBarScreen.BookSearch.route){ backStackEntry ->
            SearchScreen(
                onDetailClicked = { bookId ->
                    // To avoid duplicate navigation events
                    if (backStackEntry.getLifecycle().currentState == Lifecycle.State.RESUMED) {
                        navController.navigate("${Constants.Routes.BOOK_DETAIL_ROUTE}/$bookId")
                    }
                }
            )
        }

        // Book detail
        composable(
            route = "${Constants.Routes.BOOK_DETAIL_ROUTE}/{${Constants.Routes.BOOK_DETAIL_ID}}",
            arguments = listOf(
                navArgument(Constants.Routes.BOOK_DETAIL_ID) {
                    type = NavType.StringType
                }
            ),
        ) { backStackEntry ->
            BookDetailScreen(
                onLibraryClicked = { libraryId ->
                    if(backStackEntry.getLifecycle().currentState == Lifecycle.State.RESUMED){
                        navController.navigate("${Constants.Routes.LIBRARY_DETAIL_ROUTE}/$libraryId")
                    }
                }
            )
        }
    }
}