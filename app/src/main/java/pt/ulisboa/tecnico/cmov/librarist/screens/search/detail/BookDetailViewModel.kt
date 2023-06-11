package pt.ulisboa.tecnico.cmov.librarist.screens.search.detail

import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.ulisboa.tecnico.cmov.librarist.MapApplication
import pt.ulisboa.tecnico.cmov.librarist.data.Repository
import pt.ulisboa.tecnico.cmov.librarist.model.Book
import pt.ulisboa.tecnico.cmov.librarist.model.Library
import pt.ulisboa.tecnico.cmov.librarist.model.Notifications
import pt.ulisboa.tecnico.cmov.librarist.notifications.NotificationService
import pt.ulisboa.tecnico.cmov.librarist.notifications.NotificationsController
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants
import pt.ulisboa.tecnico.cmov.librarist.utils.LocationUtils
import javax.inject.Inject


@HiltViewModel
class BookDetailViewModel @Inject constructor(
    val repository: Repository,
    private val locationUtils: LocationUtils,
    savedStateHandle: SavedStateHandle,
    private val application: MapApplication
) : ViewModel() {

    val loading = mutableStateOf(false)
    private val barcode: String? = savedStateHandle[Constants.Routes.BOOK_DETAIL_ID]
    private val notificationController = NotificationsController(application.applicationContext)
    private val notificationService = NotificationService(repository, notificationController)
    private val _notifications = MutableStateFlow<Boolean>(false)
    val notifications: StateFlow<Boolean> = _notifications
    private val lastKnownLocation: MutableLiveData<Location> = MutableLiveData()

    var bookDetail by mutableStateOf(Book())
    var libraries by mutableStateOf(listOf<Library>())

    fun onNotificationsChanged(notifications: Boolean) {
        _notifications.value = notifications
    }

    fun onLocationChanged(location: Location){
        lastKnownLocation.value = location
    }

    init {
        barcode?.let {
            loading.value = true
            viewModelScope.launch(Dispatchers.IO) {
                // get location
                locationUtils.getLastKnownLocation(application.applicationContext)?.let { location ->
                    withContext(Dispatchers.Main) {
                        onLocationChanged(location)
                    }
                }

                // Notifications
                repository.getNotificationsForBook(barcode)?.let {
                    onNotificationsChanged(it.notifications)
                }

                // Book detail
                repository.refreshBookDetail(barcode)
                val bookDetailResult = repository.getBook(barcode)
                withContext(Dispatchers.Main) {
                    bookDetailResult?.let {
                        bookDetail = it
                    }
                }

                // Libraries
                val libs = repository.getBookLibraries(bookDetail.name)
                withContext(Dispatchers.Main) {
                    libs?.let {
                        // sorting libraries by distance
                        libraries = sortByDistance(it)
                    }
                    loading.value = false
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun notifications() {
        if (barcode != null) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    repository.addNotifications(
                        Notifications(
                            barcode = barcode,
                            notifications = notifications.value
                        )
                    )
                    notificationService.startApiPolling()
                } catch (t: Throwable) {
                    Log.d("ErrorLaunchDetail", t.toString())
                }
            }
        }
    }

    // Getting distance from the library
    private fun sortByDistance(libraries: List<Library>): List<Library>{
        if(lastKnownLocation.value != null){
            val userLocation = lastKnownLocation.value?.let { LatLng(it.latitude, lastKnownLocation.value!!.longitude) }
            val sortedLibraries = mutableListOf<Library>()

            for(lib in libraries) {
                lib.distance = userLocation?.let { locationUtils.getDistance(lib.location, it) }!!
                sortedLibraries.add(lib)
            }
            return sortedLibraries.sortedWith(compareBy { it.distance })
        }
        return emptyList()
    }
}