package pt.ulisboa.tecnico.cmov.librarist.screens.search.detail

import android.content.ContentResolver
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
import pt.ulisboa.tecnico.cmov.librarist.data.Repository
import pt.ulisboa.tecnico.cmov.librarist.model.Book
import pt.ulisboa.tecnico.cmov.librarist.model.Library
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    val repository: Repository,
    savedStateHandle: SavedStateHandle
): ViewModel()  {

    val loading = mutableStateOf(false)
    private val barcode: String? = savedStateHandle[Constants.Routes.BOOK_DETAIL_ID]

    var bookDetail by mutableStateOf(Book())

    init {
        barcode?.let {
            viewModelScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    loading.value = true
                }
                repository.refreshBookDetail(barcode)
                val bookDetailResult = repository.getBook(barcode)
                withContext(Dispatchers.Main) {
                    bookDetailResult?.let {
                        bookDetail = it
                    }
                    loading.value = false
                }
            }
        }
    }
    //TODO: converter creation for getBook() fails for some reason sometimes
    fun notifications(){
        //TODO: Implement
    }
}