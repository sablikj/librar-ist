package pt.ulisboa.tecnico.cmov.librarist.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import pt.ulisboa.tecnico.cmov.librarist.screens.MapScreen
import pt.ulisboa.tecnico.cmov.librarist.screens.SearchScreen
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants

@Composable
fun BottomNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        route = Constants.Graph.ROOT,
        startDestination = BottomBarScreen.Map.route
    ){
        // Main tab
        composable(route = BottomBarScreen.Map.route){ backStackEntry ->
            MapScreen(navController
                /*
                onDetailClicked = { launchId ->
                    // To avoid duplicate navigation events
                    if(backStackEntry.lifecycle.currentState == Lifecycle.State.RESUMED){
                        navController.navigate("${Routes.LAUNCH_DETAIL_ROUTE}/$launchId")
                    }
                }*/)
        }
        // Book search tab
        composable(route = BottomBarScreen.BookSearch.route){
            SearchScreen(navController)
        }

        // Detail
        /*
        composable(
            route = "${Routes.LAUNCH_DETAIL_ROUTE}/{${Routes.LAUNCH_DETAIL_ID}}",
            arguments = listOf(
                navArgument(Routes.LAUNCH_DETAIL_ID) {
                    type = NavType.StringType
                }
            ),
        ) {
            LaunchDetail()
        }
        wikiNavGraph(navController = navController)

         */
    }
}