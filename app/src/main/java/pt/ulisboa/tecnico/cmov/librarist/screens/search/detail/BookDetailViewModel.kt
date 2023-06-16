package pt.ulisboa.tecnico.cmov.librarist.screens.search.detail

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import pt.ulisboa.tecnico.cmov.librarist.BuildConfig
import pt.ulisboa.tecnico.cmov.librarist.MapApplication
import pt.ulisboa.tecnico.cmov.librarist.R
import pt.ulisboa.tecnico.cmov.librarist.data.Repository
import pt.ulisboa.tecnico.cmov.librarist.model.Book
import pt.ulisboa.tecnico.cmov.librarist.model.Library
import pt.ulisboa.tecnico.cmov.librarist.model.Notifications
import pt.ulisboa.tecnico.cmov.librarist.notifications.NotificationService
import pt.ulisboa.tecnico.cmov.librarist.notifications.NotificationsController
import pt.ulisboa.tecnico.cmov.librarist.utils.Constants
import pt.ulisboa.tecnico.cmov.librarist.utils.LocationUtils
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import okhttp3.Request
import okhttp3.Response

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    val repository: Repository,
    private val locationUtils: LocationUtils,
    savedStateHandle: SavedStateHandle,
    private val application: MapApplication,
) : ViewModel() {

    val loading = mutableStateOf(false)
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val barcode: String? = savedStateHandle[Constants.Routes.BOOK_DETAIL_ID]
    private val notificationController = NotificationsController(application.applicationContext)
    private val notificationService = NotificationService(application, repository, notificationController)
    private val _notifications = MutableStateFlow<Boolean>(false)
    val notifications: StateFlow<Boolean> = _notifications
    private val lastKnownLocation: MutableLiveData<Location> = MutableLiveData()

    var bookDetail by mutableStateOf(Book())
    var libraries by mutableStateOf(listOf<Library>())

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    fun connectWebSocket() {
        //websocket url - Its necessary to create endpoint using ws(non secured) or wss(secured)
        val request = Request.Builder().url("wss://your-websocket-url").build()
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Connection opened.")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Received message: $text")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Error occurred: ${t.message}")
            }
        }

        webSocket = client.newWebSocket(request, listener)
    }

    fun disconnectWebSocket() {
        webSocket?.close(1000, "Closing WebSocket")
        webSocket = null
    }
    // Override onCleared() to close the WebSocket when the ViewModel is about to be destroyed
    override fun onCleared() {
        super.onCleared()
        disconnectWebSocket()
    }

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
                repository.refreshBookDetail(application.applicationContext, barcode)
                val bookDetailResult = repository.getBook(application.applicationContext, barcode)
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

    fun shareBook(context: Context, lifecycleScope: CoroutineScope) {
        // Launching in Lifecycle scope to prevent memory leaks on cancel
        lifecycleScope.launch(Dispatchers.IO) {
            val bitmap = BitmapFactory.decodeByteArray(bookDetail.image, 0, bookDetail.image.size)

            // Create a file in the cache directory to share
            val file = File(context.cacheDir, "${bookDetail.name.replace(' ', '_')}.png")
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            // Get the Uri for the file using the FileProvider
            val fileUri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)

            // Switching to Main (UI) Thread to start the Activity
            withContext(Dispatchers.Main) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "${bookDetail.name} ${R.string.share_book_author} ${bookDetail.author}")
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    type = "image/*"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "${R.string.share_book}"))
            }
        }
    }
}