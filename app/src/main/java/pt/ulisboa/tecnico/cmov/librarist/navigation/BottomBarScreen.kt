package pt.ulisboa.tecnico.cmov.librarist.navigation

import pt.ulisboa.tecnico.cmov.librarist.R

sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val icon: Int
){
    object Map : BottomBarScreen(
        route = "mapScreen",
        title = "Map",
        icon = R.drawable.baseline_map_24
    )
    object BookSearch : BottomBarScreen(
        route = "searchScreen",
        title = "Search Books",
        icon = R.drawable.baseline_search_24
    )
}