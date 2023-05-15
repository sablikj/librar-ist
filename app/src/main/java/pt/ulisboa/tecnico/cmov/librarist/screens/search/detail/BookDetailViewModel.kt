package pt.ulisboa.tecnico.cmov.librarist.screens.search.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.ulisboa.tecnico.cmov.librarist.model.book.Book
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    //private val repository: Repository,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    val loading = mutableStateOf(false)
    private val bookId: Int? = savedStateHandle[Constants.Routes.BOOK_DETAIL_ID]

    var bookDetail by mutableStateOf(Book())
        private set

    init {
        //TODO: Implement
        /*
        bookId?.let {
            loading.value = true
            viewModelScope.launch(Dispatchers.IO) {
                repository.refreshBookDetail(bookId)
                repository.getBookDetail(it).collect {detail ->
                    withContext(Dispatchers.Main) {
                        if (detail != null) {
                            bookDetail = detail
                            loading.value = false
                        }
                    }
                }
            }
        } */
    }
}