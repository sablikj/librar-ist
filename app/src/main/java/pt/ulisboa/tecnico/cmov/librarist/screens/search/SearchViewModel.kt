package pt.ulisboa.tecnico.cmov.librarist.screens.search

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import pt.ulisboa.tecnico.cmov.librarist.model.book.Book
import javax.inject.Inject

@HiltViewModel
@ExperimentalPagingApi
class SearchViewModel@Inject constructor(
    //private val repository: Repository
): ViewModel() {

    // Search
    val searchQuery = mutableStateOf("")
    private val _searchedBooks = MutableStateFlow<PagingData<Book>>(PagingData.empty())
    val searchedBooks = _searchedBooks

    fun searchBooks(query: String) {
        //TODO: Implement
        /*
        viewModelScope.launch {
            repository.searchBooks(query = query).cachedIn(viewModelScope).collect {
                _searchedBooks.value = it
            }
        }*/
    }
}