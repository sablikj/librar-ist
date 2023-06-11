package pt.ulisboa.tecnico.cmov.librarist.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import pt.ulisboa.tecnico.cmov.librarist.data.Repository
import pt.ulisboa.tecnico.cmov.librarist.model.Book
import javax.inject.Inject

@HiltViewModel
@ExperimentalPagingApi
class SearchViewModel@Inject constructor(
    private val repository: Repository
): ViewModel() {

    // Search
    private val _searchedBooks = MutableStateFlow<PagingData<Book>>(PagingData.empty())
    val searchedBooks = _searchedBooks

    init {
        // Display everything on init
        searchBooks("")
    }

    fun searchBooks(query: String) {
        viewModelScope.launch {
            repository.searchBooks(query = query).cachedIn(viewModelScope).collect {
                _searchedBooks.value = it
            }
        }
    }
}