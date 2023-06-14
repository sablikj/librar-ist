package pt.ulisboa.tecnico.cmov.librarist.navigation

import androidx.annotation.StringRes
import pt.ulisboa.tecnico.cmov.librarist.R

sealed class BottomBarScreen(
    val route: String,
    @StringRes val title: Int,
    val icon: Int
){
    object Map : BottomBarScreen(
        route = "mapScreen",
        title = R.string.map_title,
        icon = R.drawable.baseline_map_24
    )
    object BookSearch : BottomBarScreen(
        route = "searchScreen",
        title = R.string.book_search_title,
        icon = R.drawable.baseline_search_24
    )
}