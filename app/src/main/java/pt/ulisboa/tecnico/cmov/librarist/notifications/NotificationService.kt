package pt.ulisboa.tecnico.cmov.librarist.notifications

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pt.ulisboa.tecnico.cmov.librarist.data.remote.LibraryApi
import pt.ulisboa.tecnico.cmov.librarist.model.Book


class NotificationService(
    private val libraryApi: LibraryApi,
    private val notificationsController: NotificationsController
) {
    private val pollingIntervalMillis = 5000L // 5 seconds

    private fun startApiPolling(notification: Boolean, bookTitle: String) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                while (notification) {
                    val response = async {
                        libraryApi.getBookByTitle(bookTitle)
                    }.await()
                    //handle response
                    if (response.isSuccessful && response.body() != null) {
                        val book = response.body() as Book
                        book.available?.let {
                            showNotification(
                                "Book available",
                                book.name + "is available now",
                            )
                        }

                    }
                    delay(pollingIntervalMillis)
                }
            }

        } catch (e: Throwable) {
            // Handle network or request failure
            e.printStackTrace()
            Log.d("error", "")
        }
    }

    private fun showNotification(title: String, message: String) {
        // Create and show the notification
        notificationsController.showNotification(title, message)
    }


}