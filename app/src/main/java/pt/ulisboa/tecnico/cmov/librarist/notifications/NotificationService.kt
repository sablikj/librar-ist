package pt.ulisboa.tecnico.cmov.librarist.notifications

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pt.ulisboa.tecnico.cmov.librarist.data.Repository


class NotificationService(
    private val repository: Repository,
    private val notificationsController: NotificationsController
) {
    private val pollingIntervalMillis = 5000L // 5 seconds

    fun startApiPolling(notification: Boolean, barcode: String): Boolean {
        var result = false;
        var favs = repository.getFavouriteLibraryIds()
        try {
            CoroutineScope(Dispatchers.IO).launch {
                while (notification) {
                    val response = repository.getBookLib(barcode)?.let { bookLibs ->
                        for (book in bookLibs) {
                            if (book.available && favs.contains(book.libraryId)) {
                                val bookname = repository.getBook(barcode)?.name
                                showNotification(
                                    "Book available",
                                    bookname + "is available now",
                                )
                                result = true
                                return@launch
                            }
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
        return result;
    }

    private fun showNotification(title: String, message: String) {
        // Create and show the notification
        notificationsController.showNotification(title, message)
    }


}