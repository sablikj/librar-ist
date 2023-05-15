package pt.ulisboa.tecnico.cmov.librarist.screens.map.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import pt.ulisboa.tecnico.cmov.librarist.model.library.Library
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants
import javax.inject.Inject

@HiltViewModel
class LibraryDetailViewModel @Inject constructor(
    //private val repository: Repository,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    val loading = mutableStateOf(false)
    private val libraryId: Int? = savedStateHandle[Constants.Routes.LIBRARY_DETAIL_ID]

    var libraryDetail by mutableStateOf(Library())
        private set

    init {
        //TODO: Implement
        /*
        libraryId?.let {
            loading.value = true
            viewModelScope.launch(Dispatchers.IO) {
                repository.refreshLibraryDetail(libraryId)
                repository.getLibraryDetail(it).collect {detail ->
                    withContext(Dispatchers.Main) {
                        if (detail != null) {
                            libraryDetail = detail
                            loading.value = false
                        }
                    }
                }
            }
        } */
    }

    fun checkIn(){
        //TODO: Implement
        // Should open the camera and scan the barcode, then add to library (if unknown, create book first)
    }

    fun checkOut(){
        //TODO: Implement
        // Should open the camera and scan the barcode, then remove from library
    }

    fun addFavourite(){
        //TODO: Implement
    }

    fun removeFavourite(){
        //TODO: Implement
    }
}