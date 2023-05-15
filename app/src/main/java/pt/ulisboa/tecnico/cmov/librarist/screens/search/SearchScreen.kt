package pt.ulisboa.tecnico.cmov.librarist.screens.search

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.ExperimentalPagingApi
import coil.annotation.ExperimentalCoilApi
import pt.ulisboa.tecnico.cmov.librarist.screens.common.SearchWidget

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnrememberedMutableState")
@ExperimentalPagingApi
@ExperimentalCoilApi
@Composable
fun SearchScreen(
    onDetailClicked: (Int) -> Unit,
    searchViewModel: SearchViewModel = hiltViewModel()
) {
    var searchQuery by searchViewModel.searchQuery
    //val results = searchViewModel.searchedBooks.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            SearchWidget(
                text = searchQuery,
                onTextChange = {
                    searchQuery = it
                },
                onSearchClicked = {
                    searchViewModel.searchBooks(query = it)
                }
            ) {
                //navController.popBackStack()
            }
        },
        content = { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(all = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ){
                //TODO: Implement
                /*
                itemsIndexed(results
                ){
                        _, book ->
                    book?.let {
                        BookItem(book = book, onDetailClicked = onDetailClicked)
                    }
                }*/
            }
        }
    )
}