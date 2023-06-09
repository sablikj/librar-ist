package pt.ulisboa.tecnico.cmov.librarist.screens.search

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.ExperimentalPagingApi
import coil.annotation.ExperimentalCoilApi
import androidx.paging.compose.collectAsLazyPagingItems
import pt.ulisboa.tecnico.cmov.librarist.screens.common.BookItem

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnrememberedMutableState")
@ExperimentalPagingApi
@ExperimentalCoilApi
@Composable
fun SearchScreen(
    onDetailClicked: (String) -> Unit,
    searchViewModel: SearchViewModel = hiltViewModel()
) {
    // Searchbar
    var searchText by remember { mutableStateOf("") }
    var searchActive by remember { mutableStateOf(false) }
    val results = searchViewModel.searchedBooks.collectAsLazyPagingItems()

    Box(modifier = Modifier.fillMaxSize()) {
        SearchBar(
            modifier = Modifier.align(Alignment.TopCenter),
            query = searchText,
            onQueryChange = { query ->
                searchText = query
            },
            onSearch = {
                searchActive = false
                if(searchText != ""){
                    searchViewModel.searchBooks(searchText)
                }
            },
            active = searchActive,
            onActiveChange = {
                searchActive = it
            },
            placeholder = {
                Text(text = "Search books")
            },
            leadingIcon = {
                Icon(
                    modifier = Modifier.clickable {
                    searchViewModel.searchBooks(searchText)
                    },
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search icon"
                )
            },
            trailingIcon = {
                if (searchActive) {
                    Icon(
                        modifier = Modifier.clickable {
                            if (searchText.isNotEmpty()) {
                                searchText = ""
                            } else {
                                searchActive = false
                            }
                        },
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close icon"
                    )
                }
            },
            content = {}
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            contentPadding = PaddingValues(all = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ){
            itemsIndexed(results.itemSnapshotList
            ){
                    _, book ->
                book?.let {
                    BookItem(book = book, onDetailClicked = onDetailClicked)
                }
            }
        }
    }
}