package pt.ulisboa.tecnico.cmov.librarist.screens.search.detail

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.ulisboa.tecnico.cmov.librarist.MapApplication
import pt.ulisboa.tecnico.cmov.librarist.data.Repository
import pt.ulisboa.tecnico.cmov.librarist.model.Book
import pt.ulisboa.tecnico.cmov.librarist.notifications.NotificationService
import pt.ulisboa.tecnico.cmov.librarist.notifications.NotificationsController
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants
import javax.inject.Inject


@HiltViewModel
class BookDetailViewModel @Inject constructor(
    val repository: Repository,
    savedStateHandle: SavedStateHandle,
    private val application: MapApplication
) : ViewModel() {

    val loading = mutableStateOf(false)
    private val barcode: String? = savedStateHandle[Constants.Routes.BOOK_DETAIL_ID]
    private val notificationController = NotificationsController(application.applicationContext)
    private val notificationService = NotificationService(repository, notificationController)
    var notifications = mutableStateOf(false)


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
    fun notifications() {

        if (barcode != null && notifications.value == true) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    if (barcode != null)
                        repository.saveBooksNotifications(barcode, notifications.value)
                    var result = notificationService.startApiPolling(notifications.value, barcode)
                    if (result) {
                        // notifications.value=false
                    }
                } catch (t: Throwable) {
                    Log.d("ErrorLaunchDetail", t.toString())
                }
            }
        }
        if (barcode != null && notifications.value == false) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    if (barcode != null)
                        repository.saveBooksNotifications(barcode, notifications.value)
                } catch (t: Throwable) {
                    Log.d("ErrorLaunchDetail", t.toString())
                }
            }
        }
    }
}